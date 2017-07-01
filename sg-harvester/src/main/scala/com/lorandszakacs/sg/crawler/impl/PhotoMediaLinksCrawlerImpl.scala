package com.lorandszakacs.sg.crawler.impl

import java.net.URL

import com.lorandszakacs.sg.crawler.{DidNotFindAnyPhotoLinksOnSetPageException, PhotoMediaLinksCrawler, SessionDao}
import com.lorandszakacs.sg.http._
import com.lorandszakacs.sg.model.Photo
import com.lorandszakacs.util.future._
import com.typesafe.scalalogging.StrictLogging

import scala.util.Failure
import scala.util.control.NonFatal

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 05 Jul 2016
  *
  */
private[crawler] class PhotoMediaLinksCrawlerImpl(
  private val sGClient: SGClient,
  private val sessionDao: SessionDao
)(implicit val ec: ExecutionContext) extends PhotoMediaLinksCrawler with SGURLBuilder with StrictLogging {

  private[this] implicit var _authentication: Authentication = DefaultSGAuthentication

  override def authenticateIfNeeded()(implicit passwordProvider: PasswordProvider): Future[Authentication] = {
    if (authentication.needsRefresh) {
      logger.info("need to authenticate")
      for {
        possibleSession: Option[Session] <- sessionDao.find()
        newAuthentication: Authentication <- possibleSession match {
          case Some(session) =>
            logger.info("attempting to recreate authentication from stored session")
            val recreate = for {
              auth <- sGClient.createAuthentication(session)
            } yield auth

            val result = recreate recoverWith {
              case e: FailedToVerifyNewAuthenticationException =>
                logger.error("failed to verify stored session, defaulting to using username and password", e)
                autenticateWithUsernameAndPassword(passwordProvider)
            }

            result map { r: Authentication =>
              logger.info("successfully restored authentication")
              r
            }

          case None =>
            autenticateWithUsernameAndPassword(passwordProvider)
        }
      } yield {
        _authentication = newAuthentication
        newAuthentication
      }
    } else {
      Future.successful(authentication)
    }
  }

  private def autenticateWithUsernameAndPassword(passwordProvider: PasswordProvider): Future[Authentication] = {
    for {
      (username, plainTextPassword) <- passwordProvider.usernamePassword()
      newAuthentication <- sGClient.authenticate(username, plainTextPassword)
      _ <- sessionDao.create(newAuthentication.session)
    } yield newAuthentication
  }

  override def gatherAllPhotosFromSetPage(photoSetPageUri: URL): Future[List[Photo]] = {
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
