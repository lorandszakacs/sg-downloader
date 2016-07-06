package com.lorandszakacs.sg.crawler.impl

import com.lorandszakacs.sg.crawler.{DidNotFindAnyPhotoLinksOnSetPageException, PhotoMediaLinksCrawler}
import com.lorandszakacs.sg.http._
import com.lorandszakacs.sg.model.Photo
import com.lorandszakacs.util.monads.future.FutureUtil._
import com.typesafe.scalalogging.StrictLogging

import scala.util.Failure
import scala.util.control.NonFatal

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 05 Jul 2016
  *
  */
private[crawler] class PhotoMediaLinksCrawlerImpl(private var sGClient: SGClient)(implicit val ec: ExecutionContext) extends PhotoMediaLinksCrawler with SGURLBuilder with StrictLogging {

  private[this] implicit var _authentication: Authentication = DefaultSGAuthentication

  override def authenticateIfNeeded(username: String, plainTextPassword: String): Future[Authentication] = {
    if (authentication.needsRefresh) {
      for {
        newAuthentication <- sGClient.authenticate(username, plainTextPassword)
      } yield {
        _authentication = newAuthentication
        newAuthentication
      }
    } else {
      Future.successful(authentication)
    }
  }

  override def gatherAllPhotosFromSetPage(photoSetPageUri: String): Future[List[Photo]] = {
    for {
      photoSetPageHTML <- sGClient.getPage(photoSetPageUri)
      photos <- Future fromTry {
        SGContentParser.parsePhotos(photoSetPageHTML).recoverWith {
          case NonFatal(e) => Failure(DidNotFindAnyPhotoLinksOnSetPageException(photoSetPageUri))
        }
      }
    } yield photos
  }

  override def authentication: Authentication = _authentication
}
