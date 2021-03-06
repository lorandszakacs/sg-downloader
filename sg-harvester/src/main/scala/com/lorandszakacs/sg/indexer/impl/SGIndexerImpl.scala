package com.lorandszakacs.sg.indexer.impl

import com.lorandszakacs.util.effects._
import com.lorandszakacs.sg.contentparser.SGContentParser
import com.lorandszakacs.sg.core
import com.lorandszakacs.sg.indexer.{FailedToRepeatedlyLoadPageException, SGIndexer}
import com.lorandszakacs.sg.http._
import com.lorandszakacs.sg.model.M.{HFFactory, MFactory, SGFactory}
import com.lorandszakacs.sg.model._
import com.lorandszakacs.util.html.Html
import org.http4s.Uri
import com.lorandszakacs.util.logger._
import fs2._

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
final private[indexer] class SGIndexerImpl(val sGClient: SGClient) extends SGIndexer with SGURLBuilder {
  implicit private val logger: Logger[IO] = Logger.getLogger[IO]

  implicit private[this] val Authentication: Authentication = DefaultSGAuthentication

  private val SGsSortedByFollowers = s"${core.Domain}/profiles/girl/followers/"
  private val HFsSortedByFollowers = s"${core.Domain}/profiles/hopeful/followers/"
  private val NewestSets           = s"${core.Domain}/photos/all/recent/all/"

  /**
    * Gathers the names of all available [[SG]]s
    */
  override def gatherSGNames(limit: Int)(implicit pc: PatienceConfig): IO[List[Name]] = {
    def isEndPage(html: Html) = {
      val PartialPageLoadingEndMarker = "Sorry, no users match your criteria."
      html.document.body().text().take(PartialPageLoadingEndMarker.length).contains(PartialPageLoadingEndMarker)
    }

    loadPageRepeatedly[Name](
      uri             = SGsSortedByFollowers,
      offsetStep      = 12,
      cutOffLimit     = limit,
      parsingFunction = SGContentParser.gatherSGNames,
      isFinalPage     = isEndPage,
    )
  }

  /**
    * Gathers the names of all available [[HF]]s
    */
  override def gatherHFNames(limit: Int)(implicit pc: PatienceConfig): IO[List[Name]] = {
    def isEndPage(html: Html) = {
      val PartialPageLoadingEndMarker = "Sorry, no users match your criteria."
      html.document.body().text().take(PartialPageLoadingEndMarker.length).contains(PartialPageLoadingEndMarker)
    }

    loadPageRepeatedly[Name](
      uri             = HFsSortedByFollowers,
      offsetStep      = 12,
      cutOffLimit     = limit,
      parsingFunction = SGContentParser.gatherHFNames,
      isFinalPage     = isEndPage,
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
    mf:   MFactory[T],
  )(name: Name)(implicit pc: PatienceConfig): IO[T] = {
    val pageURL = photoSetsPageURL(name)
    for {
      sets <- loadPageRepeatedly[PhotoSet](
        uri             = pageURL,
        offsetStep      = 9,
        cutOffLimit     = Int.MaxValue,
        parsingFunction = SGContentParser.gatherPhotoSetsForM,
        isFinalPage     = isEndPageForMIndexing,
      )
      _ <- logger.info(s"gathered all sets for ${mf.name} ${name.name}. #sets: ${sets.length}")
    } yield mf(photoSetURL = pageURL, name = name, photoSets = sets)
  }

  override def gatherPhotoSetInformationForName(name: Name)(implicit pc: PatienceConfig): IO[M] = {
    val pageURL = photoSetsPageURL(name)
    for {
      sets <- loadPageRepeatedly[PhotoSet](
        uri             = pageURL,
        offsetStep      = 9,
        cutOffLimit     = Int.MaxValue,
        parsingFunction = SGContentParser.gatherPhotoSetsForM,
        isFinalPage     = isEndPageForMIndexing,
      )
      isHF = sets.exists(_.isHFSet.contains(true))
      mf   = if (isHF) HFFactory else SGFactory
      _ <- logger.info(s"gathered all sets for ${mf.name} ${name.name}. #sets: ${sets.length}")
    } yield mf(photoSetURL = pageURL, name = name, photoSets = sets)
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
  private[impl] def gatherAllNewMsAndOnlyTheirLatestSet(
    limit:              Int,
    lastProcessedIndex: Option[LastProcessedMarker],
  )(
    implicit
    pc: PatienceConfig,
  ): IO[List[M]] = {
    def isFinalPage(html: Html) = {
      val PartialPageLoadingEndMarker = "No photos available."
      html.document.body().text().take(PartialPageLoadingEndMarker.length).contains(PartialPageLoadingEndMarker)
    }

    def isLastPageVisted(ms: List[M]): Boolean = {
      lastProcessedIndex match {
        case None => false
        case Some(lpi) =>
          ms.exists(_.photoSets.exists(ps => ps.id == lpi.lastPhotoSetID || ps.date.isBefore(lpi.photoSet.date)))
      }

    }

    loadPageRepeatedly[M](
      uri             = NewestSets,
      offsetStep      = 24,
      cutOffLimit     = limit,
      parsingFunction = SGContentParser.gatherNewestPhotoSets,
      isFinalPage     = isFinalPage,
      stopOnPage      = isLastPageVisted,
    ).map { ms =>
      if (lastProcessedIndex.isEmpty)
        ms
      else
        ms.takeWhile { m =>
          val photoset = m.photoSets.headOption.getOrElse(
            throw new AssertionError("... tried to get lastPhotoSet, of a NewestMPhotoSet, but it did not exist"),
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
    implicit pc:                                         PatienceConfig,
  ): IO[List[M]] = {
    for {
      msWithOnlyOneSet <- gatherAllNewMsAndOnlyTheirLatestSet(limit, lastProcessedIndex)
      sgHF = msWithOnlyOneSet.distinctById.group
      sgs <- sgHF.sgs.traverse { sg =>
        pc.throttleAfter {
          this.gatherPhotoSetInformationForM(M.SGFactory)(sg.name)
        }
      }
      hfs <- sgHF.hfs.traverse { hf =>
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
    * @param stopOnPage
    *   Predicate to decide if loading should stop based on the results
    *   of a successful ``parsingFunction``
    *   If this function returns true, then loading stops and results gathered so far are returned
    * @param isFinalPage
    *   Determines if the loaded page is the "last page".
    *   If this function returns true, then loading stops and results gathered so far are returned
    * @param uri
    * Assumed to not have any ``offset`` HTTP parameters
    * when passed to this function. i.e.
    * it should be ``/photos`` not ``/photos?offset=x``.
    *
    */
  private def loadPageRepeatedly[T](
    uri:             Uri,
    offsetStep:      Int,
    cutOffLimit:     Int,
    parsingFunction: Html => Try[List[T]],
    isFinalPage:     Html => Boolean,
    stopOnPage:      List[T] => Boolean = (_: List[T]) => false,
  )(
    implicit
    pc: PatienceConfig,
  ): IO[List[T]] = {

    def offsetUri(uri: Uri, offset: Int): Uri =
      Uri.unsafeFromString(s"$uri?partial=true&offset=$offset")

    /*
     * This filter is exclusive, we do not want to :
     * - include the final page (usually empty) in the result set
     * - cutofflimit is already lower then where we're at, so we exclude from result set
     */
    def keepThisPageExclusive(t: (Int, Html, List[T])): Boolean = {
      val (offset, html, _) = t

      (offset <= cutOffLimit) && !isFinalPage(html)
    }

    /**
      * This is an inclusive filter, we want to keep the page on which
      * we ought to stop on (for checking latest page visited) for example
      */
    def keepThisPageInclusive(t: (Int, Html, List[T])): Boolean = {
      val (_, _, list) = t

      !stopOnPage(list)
    }

    val htmlPages: Stream[IO, (Int, Html)] = Stream
      .range(start = 0, stopExclusive = cutOffLimit, by = offsetStep)
      .evalMap { offset =>
        for {
          newURI <- IO.pure(offsetUri(uri, offset))
          _      <- logger.info(s"load repeatedly: step=$offsetStep [$newURI]")
          html   <- pc.throttleAfter(sGClient.getPage(newURI))
        } yield (offset, html)
      }

    val parsedPages: Stream[IO, (Int, Html, List[T])] =
      htmlPages.evalMap { p =>
        val (offset, html) = p
        IO.fromTry(parsingFunction(html))
          .adaptError {
            case NonFatal(e) => FailedToRepeatedlyLoadPageException(offset, e)
          }
          .map(ts => (offset, html, ts))
      }

    val relevantPages = parsedPages.takeThrough(keepThisPageInclusive).takeWhile(keepThisPageExclusive)

    val result: Stream[IO, List[T]] = relevantPages.map(_._3)

    result.compile.toList.map(_.flatten)
  }

}
