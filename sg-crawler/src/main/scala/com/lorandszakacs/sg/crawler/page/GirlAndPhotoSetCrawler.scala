package com.lorandszakacs.sg.crawler.page

import akka.http.scaladsl.model.Uri
import com.lorandszakacs.sg.http.SGClient
import com.lorandszakacs.sg.model._
import com.lorandszakacs.util.html.Html
import com.typesafe.scalalogging.StrictLogging

import scala.collection.mutable.ListBuffer
import scala.concurrent.{Await, Future, ExecutionContext}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

/**
  *
  * All public methods ensure that the URIs are fully qualified, and not relative!
  *
  * This crawler only fetches a complete list of [[SuicideGirl]], and/or [[Hopeful]]s with
  * all their [[PhotoSet]]s, but none of the media links
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 03 Jul 2016
  *
  */
final class GirlAndPhotoSetCrawler(sGClient: SGClient)(implicit val ec: ExecutionContext) extends StrictLogging {

  private val SGsSortedByFollowers = "https://www.suicidegirls.com/profiles/girl/followers/"
  private val HopefulsSortedByFollowers = "https://www.suicidegirls.com/profiles/hopeful/followers/"

  /**
    * Gathers the names of all available [[SuicideGirl]]s
    */
  def gatherSGNames(limit: Int): Future[List[String]] = {
    def isEndPage(html: Html) = {
      val PartialPageLoadingEndMarker = "Sorry, no users match your criteria."
      html.document.body().text().take(PartialPageLoadingEndMarker.length).contains(PartialPageLoadingEndMarker)
    }

    loadPageRepeatedly[String](
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
  def gatherHopefulNames(limit: Int): Future[List[String]] = {
    def isEndPage(html: Html) = {
      val PartialPageLoadingEndMarker = "Sorry, no users match your criteria."
      html.document.body().text().take(PartialPageLoadingEndMarker.length).contains(PartialPageLoadingEndMarker)
    }

    loadPageRepeatedly[String](
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
  def gatherPhotoSetInformationFor(modelName: String): Future[List[PhotoSet]] = {
    def isEndPage(html: Html) = {
      val PartialPageLoadingEndMarker = "No photos available."
      html.document.body().text().take(PartialPageLoadingEndMarker.length).contains(PartialPageLoadingEndMarker)
    }
    val pageURI = photoSetsPageUri(modelName)

    for {
      setsWithRelativeURIs <- loadPageRepeatedly[PhotoSet](
        uri = pageURI,
        offsetStep = 9,
        parsingFunction = SGContentParser.gatherPhotoSets,
        isEndPage = isEndPage
      )
    } yield setsWithRelativeURIs map { ph =>
      ph.copy(url = makeFullPathURI(ph.url).toString)
    }
  }

  /**
    * This is the URI to the page containing all [[PhotoSet]] of a [[Model]]
    */
  private def photoSetsPageUri(modelName: String): Uri =
    Uri(s"https://www.suicidegirls.com/girls/${modelName.toLowerCase}/photos/view/photosets/")

  private def makeFullPathURI(uri: String): Uri = {
    if (uri.startsWith("/")) {
      Uri(s"https://www.suicidegirls.com$uri")
    } else {
      Uri(s"https://www.suicidegirls.com/$uri")
    }
  }

  private def makeFullPathURI(uri: Uri): Uri = {
    makeFullPathURI(uri.toString)
  }

  /**
    *
    * @param uri
    * Assumed to not have any ``offset`` HTTP parameter
    * @param offsetStep
    * @param parsingFunction
    * @param isEndPage
    * @param cutOffLimit
    * @param throttle
    * @return
    */
  private def loadPageRepeatedly[T](
    uri: Uri,
    offsetStep: Int,
    parsingFunction: Html => Try[List[T]],
    isEndPage: Html => Boolean,
    throttle: FiniteDuration = 100 millis,
    cutOffLimit: Int = Int.MaxValue
  ): Future[List[T]] = {

    def offsetUri(uri: Uri, offset: Int) =
      Uri(s"$uri?partial=true&offset=$offset")

    sGClient.getPage(offsetUri(uri, 0)) map { firstHtml =>
      val photoSetUris = ListBuffer[T]()
      photoSetUris ++= parsingFunction(firstHtml).get

      var offset = offsetStep
      var stop = false
      do {
        Thread.sleep(throttle.toMillis)
        val newPage = Await.result(sGClient.getPage(offsetUri(uri, offset)), 1 minute)
        offset += offsetStep
        if (isEndPage(newPage) || offset > cutOffLimit) {
          stop = true
        } else {
          logger.info(s"load repeatedly: $offset $offsetStep")
          parsingFunction(newPage) match {
            case Success(s) =>
              photoSetUris ++= s
            case Failure(e) =>
              throw FailedToRepeatedlyLoadPageException(offset, e)
          }
        }
      } while (!stop)
      photoSetUris.toList
    }
  }
}
