package com.lorandszakacs.sg.crawler.impl

import akka.http.scaladsl.model.Uri
import com.lorandszakacs.sg.crawler.{ModelAndPhotoSetCrawler, FailedToRepeatedlyLoadPageException}
import com.lorandszakacs.sg.http.{SGURLBuilder, PatienceConfig, SGClient}
import com.lorandszakacs.sg.model._
import com.lorandszakacs.util.html.Html
import com.typesafe.scalalogging.StrictLogging

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
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
final class ModelAndPhotoSetCrawlerImpl(val sGClient: SGClient)(implicit val ec: ExecutionContext) extends ModelAndPhotoSetCrawler with SGURLBuilder with StrictLogging {

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
  override def gatherHopefulNames(limit: Int)(implicit pc: PatienceConfig): Future[List[ModelName]] = {
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
    * All elements of the list will have: [[PhotoSet.photos.isEmpty]]
    */
  override def gatherPhotoSetInformationFor(modelName: ModelName)(implicit pc: PatienceConfig): Future[List[PhotoSet]] = {
    def isEndPage(html: Html) = {
      val PartialPageLoadingEndMarker = "No photos available."
      html.document.body().text().take(PartialPageLoadingEndMarker.length).contains(PartialPageLoadingEndMarker)
    }
    val pageURI = photoSetsPageURL(modelName)

    for {
      sets <- loadPageRepeatedly[PhotoSet](
        uri = pageURI,
        offsetStep = 9,
        parsingFunction = SGContentParser.gatherPhotoSetsForModel,
        isEndPage = isEndPage
      )
    } yield sets
  }


  override def gatherNewestModelInformation(limit: Int, lastProcessedIndex: Option[LastProcessedIndex])(implicit pc: PatienceConfig): Future[List[Model]] = {
    def isEndPage(html: Html) = {
      val PartialPageLoadingEndMarker = "No photos available."
      html.document.body().text().take(PartialPageLoadingEndMarker.length).contains(PartialPageLoadingEndMarker)
    }
    def isEndInput(models: List[Model]): Boolean = {
      lastProcessedIndex match {
        case None => false
        case Some(lpi) => models.exists(_.photoSets.exists(_.id == lpi.lastPhotoSetID))
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
    *
    * @param uri
    * Assumed to not have any ``offset`` HTTP parameter
    * @param offsetStep
    * @param parsingFunction
    * @param isEndPage
    * @param cutOffLimit
    * @return
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
      val photoSetUris = ListBuffer[T]()
      photoSetUris ++= parsingFunction(firstHtml).get

      var offset = offsetStep
      var stop = false
      do {
        Thread.sleep(pc.throttle.toMillis)
        val newPage = Await.result(sGClient.getPage(offsetUri(uri, offset)), 1 minute)
        offset += offsetStep
        if (isEndPage(newPage) || offset > cutOffLimit) {
          stop = true
        } else {
          logger.info(s"load repeatedly: $offset $offsetStep")
          parsingFunction(newPage) match {
            case Success(s) =>
              photoSetUris ++= s
              if (isEndInput(s)) {
                stop = true
              }
            case Failure(e) =>
              throw FailedToRepeatedlyLoadPageException(offset, e)
          }
        }
      } while (!stop)
      photoSetUris.toList
    }
  }

  /**
    * This page contains elements like the following which need to be parsed
    *
    * {{{
    *
    * }}}
    */
  override def gatherNewestSets(limit: Int)(implicit pc: PatienceConfig): Future[List[Model]] = ???
}
