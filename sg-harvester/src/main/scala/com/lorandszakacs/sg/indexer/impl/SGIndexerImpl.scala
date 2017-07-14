package com.lorandszakacs.sg.indexer.impl

import akka.http.scaladsl.model.Uri
import com.lorandszakacs.sg.contentparser.SGContentParser
import com.lorandszakacs.sg.indexer.{FailedToRepeatedlyLoadPageException, SGIndexer}
import com.lorandszakacs.sg.http._
import com.lorandszakacs.sg.model.Model.ModelFactory
import com.lorandszakacs.sg.model._
import com.lorandszakacs.util.future._
import com.lorandszakacs.util.list._
import com.lorandszakacs.util.html.Html

import com.typesafe.scalalogging.StrictLogging

import scala.collection.mutable.ListBuffer
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

/**
  *
  * All public methods ensure that the URIs are fully qualified, and not relative!
  *
  * This crawler only fetches a complete list of [[SuicideGirl]], and/or [[Hopeful]]s with
  * all their [[PhotoSet]]s, but none of the photo links
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 03 Jul 2016
  *
  */
private[indexer] final class SGIndexerImpl(val sGClient: SGClient)(implicit val ec: ExecutionContext) extends SGIndexer with SGURLBuilder with StrictLogging {

  private[this] implicit val Authentication: Authentication = DefaultSGAuthentication

  private val SGsSortedByFollowers = "https://www.suicidegirls.com/profiles/girl/followers/"
  private val HopefulsSortedByFollowers = "https://www.suicidegirls.com/profiles/hopeful/followers/"
  private val NewestSets = "https://www.suicidegirls.com/photos/all/recent/all/"

  /**
    * Gathers the names of all available [[SuicideGirl]]s
    */
  override def gatherSGNames(limit: Int)(implicit pc: PatienceConfig): Future[List[ModelName]] = {
    def isEndPage(html: Html) = {
      val PartialPageLoadingEndMarker = "Sorry, no users match your criteria."
      html.document.body().text().take(PartialPageLoadingEndMarker.length).contains(PartialPageLoadingEndMarker)
    }

    loadPageRepeatedly[ModelName](
      uri = SGsSortedByFollowers,
      offsetStep = 12,
      parsingFunction = SGContentParser.gatherSGNames,
      isEndPage = isEndPage,
      cutOffLimit = limit
    )
  }


  /**
    * Gathers the names of all available [[Hopeful]]s
    */
  override def gatherHFNames(limit: Int)(implicit pc: PatienceConfig): Future[List[ModelName]] = {
    def isEndPage(html: Html) = {
      val PartialPageLoadingEndMarker = "Sorry, no users match your criteria."
      html.document.body().text().take(PartialPageLoadingEndMarker.length).contains(PartialPageLoadingEndMarker)
    }

    loadPageRepeatedly[ModelName](
      uri = HopefulsSortedByFollowers,
      offsetStep = 12,
      parsingFunction = SGContentParser.gatherHopefulNames,
      isEndPage = isEndPage,
      cutOffLimit = limit
    )
  }

  /**
    *
    * At the time of writing, there are at most 9 sets displayed per such a page.
    *
    * Check:
    * https://www.suicidegirls.com/girls/dwam/photos/view/photosets/
    * To see if this still holds true.
    *
    * @return
    * the [[PhotoSet]]s of the given model.
    * All elements of the list will have: [[PhotoSet.photos.isEmpty]], and [[PhotoSet.url]] will be a full path URL.
    */
  @scala.deprecated("will be made private", "now")
  override def gatherPhotoSetInformationForModel[T <: Model](mf: ModelFactory[T])(modelName: ModelName)(implicit pc: PatienceConfig): Future[T] = {
    def isEndPage(html: Html) = {
      val PartialPageLoadingEndMarker = "No photos available."
      html.document.body().text().take(PartialPageLoadingEndMarker.length).contains(PartialPageLoadingEndMarker)
    }

    val pageURL = photoSetsPageURL(modelName)

    for {
      sets <- loadPageRepeatedly[PhotoSet](
        uri = pageURL,
        offsetStep = 9,
        parsingFunction = SGContentParser.gatherPhotoSetsForModel,
        isEndPage = isEndPage
      )
    } yield {
      logger.info(s"gathered all sets for ${mf.name} ${modelName.name}. #sets: ${sets.length}")
      mf(photoSetURL = pageURL, name = modelName, photoSets = sets)
    }
  }


  /**
    *
    * Gathers information about the latest published sets from:
    * https://www.suicidegirls.com/photos/all/recent/all/
    *
    * The amount of crawling is limited by the absolute limit, or by the set identified by [[LastProcessedMarker.lastPhotoSetID]]
    * This last set is not included in the results.
    *
    * Returns a [[Model]] with only one [[Model.photoSets]], the one that shows up on the page.
    */
  @scala.deprecated("will be made private", "now")
  override def gatherAllNewModelsAndOnlyTheirLatestSet(limit: Int, lastProcessedIndex: Option[LastProcessedMarker])(implicit pc: PatienceConfig): Future[List[Model]] = {
    def isEndPage(html: Html) = {
      val PartialPageLoadingEndMarker = "No photos available."
      html.document.body().text().take(PartialPageLoadingEndMarker.length).contains(PartialPageLoadingEndMarker)
    }

    def isEndInput(models: List[Model]): Boolean = {
      lastProcessedIndex match {
        case None => false
        case Some(lpi) =>
          models.exists(_.photoSets.exists(_.id == lpi.lastPhotoSetID))
      }

    }

    loadPageRepeatedly[Model](
      uri = NewestSets,
      offsetStep = 24,
      parsingFunction = SGContentParser.gatherNewestPhotoSets,
      isEndPage = isEndPage,
      isEndInput = isEndInput,
      cutOffLimit = limit
    ) map { models =>
      if (lastProcessedIndex.isEmpty)
        models
      else
        models.takeWhile { m =>
          val photoset = m.photoSets.headOption.getOrElse(throw new AssertionError("... tried to get lastPhotoSet, of a NewestModelPhotoSet, but it did not exist"))
          lastProcessedIndex.isEmpty || (photoset.id != lastProcessedIndex.get.lastPhotoSetID)
        }
    }
  }

  /**
    * Eliminates duplicate [[Model]], sometimes it happens that a model has two sets on the newest page, especially
    * if we wait a lot of time between updates.
    *
    * A composite operation of [[gatherAllNewModelsAndOnlyTheirLatestSet]] and
    * [[gatherPhotoSetInformationForModel]] gotten for that model.
    *
    */
  override def gatherAllNewModelsAndAllTheirPhotoSets(limit: Int, lastProcessedIndex: Option[LastProcessedMarker])(implicit pc: PatienceConfig): Future[List[Model]] = {
    for {
      modelsWithOnlyOneSet: List[Model] <- gatherAllNewModelsAndOnlyTheirLatestSet(limit, lastProcessedIndex)
      sgHF = modelsWithOnlyOneSet.distinctById.group
      sgs <- Future.serialize(sgHF.sgs) { sg =>
        pc.throttleAfter {
          this.gatherPhotoSetInformationForModel(Model.SuicideGirlFactory)(sg.name)
        }
      }
      hfs <- Future.serialize(sgHF.hfs) { hf =>
        pc.throttleAfter {
          this.gatherPhotoSetInformationForModel(Model.HopefulFactory)(hf.name)
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
    uri: Uri,
    offsetStep: Int,
    parsingFunction: Html => Try[List[T]],
    isEndPage: Html => Boolean,
    cutOffLimit: Int = Int.MaxValue,
    isEndInput: List[T] => Boolean = { ls: List[T] => false }
  )(implicit pc: PatienceConfig): Future[List[T]] = {

    def offsetUri(uri: Uri, offset: Int) =
      Uri(s"$uri?partial=true&offset=$offset")

    sGClient.getPage(offsetUri(uri, 0)) map { firstHtml =>
      val result = ListBuffer[T]()
      val firstBatch = parsingFunction(firstHtml).get
      var offset = offsetStep

      var stop = false
      result ++= firstBatch

      if (isEndInput(firstBatch)) {
        stop = true
      }

      while (!stop) {
        Thread.sleep(pc.throttle.toMillis)
        val newPage = sGClient.getPage(offsetUri(uri, offset)).await()
        offset += offsetStep
        if (isEndPage(newPage) || offset > cutOffLimit) {
          stop = true
        } else {
          logger.info(s"load repeatedly: currentOffset=$offset; step=$offsetStep")
          parsingFunction(newPage) match {
            case Success(s) =>
              result ++= s
              if (isEndInput(s)) {
                stop = true
              }
            case Failure(e) =>
              throw FailedToRepeatedlyLoadPageException(offset, e)
          }
        }
      }

      result.toList
    }
  }
}
