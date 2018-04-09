package com.lorandszakacs.sg.indexer.impl

import com.lorandszakacs.util.effects._

import akka.http.scaladsl.model.Uri
import com.lorandszakacs.sg.contentparser.SGContentParser
import com.lorandszakacs.sg.core
import com.lorandszakacs.sg.indexer.{FailedToRepeatedlyLoadPageException, SGIndexer}
import com.lorandszakacs.sg.http._
import com.lorandszakacs.sg.model.M.{HFFactory, MFactory, SGFactory}
import com.lorandszakacs.sg.model._
import com.lorandszakacs.util.html.Html
import com.typesafe.scalalogging.StrictLogging

import scala.collection.mutable.ListBuffer

/**
  *
  * All public methods ensure that the URIs are fully qualified, and not relative!
  *
  * This crawler only fetches a complete list of [[SG]], and/or [[HF]]s with
  * all their [[PhotoSet]]s, but none of the photo links
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 03 Jul 2016
  *
  */
private[indexer] final class SGIndexerImpl(val sGClient: SGClient)
    extends SGIndexer with SGURLBuilder with StrictLogging {

  private[this] implicit val Authentication: Authentication = DefaultSGAuthentication

  private val SGsSortedByFollowers = s"${core.Domain}/profiles/girl/followers/"
  private val HFsSortedByFollowers = s"${core.Domain}/profiles/hopeful/followers/"
  private val NewestSets           = s"${core.Domain}/photos/all/recent/all/"

  /**
    * Gathers the names of all available [[SG]]s
    */
  override def gatherSGNames(limit: Int)(implicit pc: PatienceConfig): Task[List[Name]] = {
    def isEndPage(html: Html) = {
      val PartialPageLoadingEndMarker = "Sorry, no users match your criteria."
      html.document.body().text().take(PartialPageLoadingEndMarker.length).contains(PartialPageLoadingEndMarker)
    }

    loadPageRepeatedly[Name](
      uri             = SGsSortedByFollowers,
      offsetStep      = 12,
      parsingFunction = SGContentParser.gatherSGNames,
      isEndPage       = isEndPage,
      cutOffLimit     = limit
    )
  }

  /**
    * Gathers the names of all available [[HF]]s
    */
  override def gatherHFNames(limit: Int)(implicit pc: PatienceConfig): Task[List[Name]] = {
    def isEndPage(html: Html) = {
      val PartialPageLoadingEndMarker = "Sorry, no users match your criteria."
      html.document.body().text().take(PartialPageLoadingEndMarker.length).contains(PartialPageLoadingEndMarker)
    }

    loadPageRepeatedly[Name](
      uri             = HFsSortedByFollowers,
      offsetStep      = 12,
      parsingFunction = SGContentParser.gatherHFNames,
      isEndPage       = isEndPage,
      cutOffLimit     = limit
    )
  }

  private def isEndPageForMIndexing(html: Html): Boolean = {
    val PartialPageLoadingEndMarker = "No photos available."
    html.document.body().text().take(PartialPageLoadingEndMarker.length).contains(PartialPageLoadingEndMarker)
  }

  /**
    *
    * At the time of writing, there are at most 9 sets displayed per such a page.
    *
    * Check:
    * $domain/girls/dwam/photos/view/photosets/
    * To see if this still holds true.
    *
    * @return
    * the [[PhotoSet]]s of the given M.
    * All elements of the list will have: [[PhotoSet.photos.isEmpty]], and [[PhotoSet.url]] will be a full path URL.
    */
  override def gatherPhotoSetInformationForM[T <: M](
    mf:   MFactory[T]
  )(name: Name)(implicit pc: PatienceConfig): Task[T] = {
    val pageURL = photoSetsPageURL(name)
    for {
      sets <- loadPageRepeatedly[PhotoSet](
        uri             = pageURL,
        offsetStep      = 9,
        parsingFunction = SGContentParser.gatherPhotoSetsForM,
        isEndPage       = isEndPageForMIndexing
      )
    } yield {
      logger.info(s"gathered all sets for ${mf.name} ${name.name}. #sets: ${sets.length}")
      mf(photoSetURL = pageURL, name = name, photoSets = sets)
    }
  }

  override def gatherPhotoSetInformationForName(name: Name)(implicit pc: PatienceConfig): Task[M] = {
    val pageURL = photoSetsPageURL(name)
    for {
      sets <- loadPageRepeatedly[PhotoSet](
        uri             = pageURL,
        offsetStep      = 9,
        parsingFunction = SGContentParser.gatherPhotoSetsForM,
        isEndPage       = isEndPageForMIndexing
      )
      isHF = sets.exists(_.isHFSet.contains(true))
      mf   = if (isHF) HFFactory else SGFactory
    } yield {
      logger.info(s"gathered all sets for ${mf.name} ${name.name}. #sets: ${sets.length}")
      mf(photoSetURL = pageURL, name = name, photoSets = sets)
    }
  }

  /**
    *
    * Gathers information about the latest published sets from:
    * $domain/photos/all/recent/all/
    *
    * The amount of crawling is limited by the absolute limit, or by the set identified by [[LastProcessedMarker.lastPhotoSetID]]
    * This last set is not included in the results.
    *
    * Returns a [[M]] with only one [[M.photoSets]], the one that shows up on the page.
    */
  private[impl] def gatherAllNewMsAndOnlyTheirLatestSet(limit: Int, lastProcessedIndex: Option[LastProcessedMarker])(
    implicit pc:                                               PatienceConfig
  ): Task[List[M]] = {
    def isEndPage(html: Html) = {
      val PartialPageLoadingEndMarker = "No photos available."
      html.document.body().text().take(PartialPageLoadingEndMarker.length).contains(PartialPageLoadingEndMarker)
    }

    def isEndInput(ms: List[M]): Boolean = {
      lastProcessedIndex match {
        case None => false
        case Some(lpi) =>
          ms.exists(_.photoSets.exists(_.id == lpi.lastPhotoSetID))
      }

    }

    loadPageRepeatedly[M](
      uri             = NewestSets,
      offsetStep      = 24,
      parsingFunction = SGContentParser.gatherNewestPhotoSets,
      isEndPage       = isEndPage,
      isEndInput      = isEndInput,
      cutOffLimit     = limit
    ) map { ms =>
      if (lastProcessedIndex.isEmpty)
        ms
      else
        ms.takeWhile { m =>
          val photoset = m.photoSets.headOption.getOrElse(
            throw new AssertionError("... tried to get lastPhotoSet, of a NewestMPhotoSet, but it did not exist")
          )
          lastProcessedIndex.isEmpty || (photoset.id != lastProcessedIndex.get.lastPhotoSetID)
        }
    }
  }

  /**
    *
    * Reindexes the Ms that have a set on the page:
    * $domain/photos/all/recent/all/
    *
    * The amount of crawling is limited by the absolute limit, or by the set identified by [[LastProcessedMarker.lastPhotoSetID]]
    * This last set is not included in the results.
    *
    * Eliminates duplicate [[M]], sometimes it happens that a M has two sets on the newest page, especially
    * if we wait a lot of time between updates.
    *
    * @return
    * All Ms that have been gathered with fully indexed information, i.e. all their photosets, but no photo information
    */
  override def gatherAllNewMsAndAllTheirPhotoSets(limit: Int, lastProcessedIndex: Option[LastProcessedMarker])(
    implicit pc:                                         PatienceConfig
  ): Task[List[M]] = {
    for {
      msWithOnlyOneSet <- gatherAllNewMsAndOnlyTheirLatestSet(limit, lastProcessedIndex)
      sgHF = msWithOnlyOneSet.distinctById.group
      sgs <- Task.serialize(sgHF.sgs) { sg =>
        pc.throttleAfter {
          this.gatherPhotoSetInformationForM(M.SGFactory)(sg.name)
        }
      }
      hfs <- Task.serialize(sgHF.hfs) { hf =>
        pc.throttleAfter {
          this.gatherPhotoSetInformationForM(M.HFFactory)(hf.name)
        }
      }
    } yield sgs ++ hfs
  }

  /**
    *
    * Basic function that repeatedly hits the "load more" button,
    * or something equivalent. It is usually modeled with the URL
    * parameters: ``partial=true&offset=10``, where we start with
    * and offset of zero, and increment it until ``isEndPage``, or
    * ``isEndInput`` are true, or when we hit the ``cutOffLimit``
    *
    * @param uri
    * Assumed to not have any ``offset`` HTTP parameters
    * when passed to this function. i.e.
    * it should be ``/photos`` not ``/photos?offset=x``.
    *
    */
  private def loadPageRepeatedly[T](
    uri:             Uri,
    offsetStep:      Int,
    parsingFunction: Html => Try[List[T]],
    isEndPage:       Html => Boolean,
    cutOffLimit:     Int = Int.MaxValue,
    isEndInput:      List[T] => Boolean = (ls: List[T]) => false
  )(
    implicit
    pc: PatienceConfig
  ): Task[List[T]] = {

    def offsetUri(uri: Uri, offset: Int) =
      Uri(s"$uri?partial=true&offset=$offset")

    sGClient.getPage(offsetUri(uri, 0)) map { firstHtml =>
      val result     = ListBuffer[T]()
      val firstBatch = parsingFunction(firstHtml).get
      var offset     = offsetStep

      var stop = false
      result ++= firstBatch

      if (isEndInput(firstBatch)) {
        stop = true
      }

      while (!stop) {
        val newURI  = offsetUri(uri, offset)
        val newPage = sGClient.getPage(newURI).unsafeSyncGet()(Scheduler.global) //FIXME: write in a pure way
        offset += offsetStep
        if (isEndPage(newPage) || offset > cutOffLimit) {
          stop = true
        }
        else {
          logger.info(s"load repeatedly: step=$offsetStep [$newURI]")
          pc.throttleThread()
          parsingFunction(newPage) match {
            case TrySuccess(s) =>
              result ++= s
              if (isEndInput(s)) {
                stop = true
              }
            case TryFailure(e) =>
              throw FailedToRepeatedlyLoadPageException(offset, e)
          }
        }
      }

      result.toList
    }
  }
}
