package com.lorandszakacs.sg.reifier.impl

import java.net.URL

import com.lorandszakacs.sg.contentparser.SGContentParser
import com.lorandszakacs.sg.http._
import com.lorandszakacs.sg.model.Model.{HopefulFactory, ModelFactory, SuicideGirlFactory}
import com.lorandszakacs.sg.model.{Hopeful, Model, ModelName, ModelUpdater, Photo, SuicideGirl}
import com.lorandszakacs.sg.reifier.{DidNotFindAnyPhotoLinksOnSetPageException, SGReifier}
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
private[reifier] class SGReifierImpl(
  private val sGClient: SGClient,
  private val sessionDao: SessionDaoImpl
)(implicit val ec: ExecutionContext) extends SGReifier with SGURLBuilder with StrictLogging {

  private[this] implicit var _authentication: Authentication = DefaultSGAuthentication

  override def authenticateIfNeeded()(implicit passwordProvider: PasswordProvider): Future[Authentication] = {
    if (authentication.needsRefresh) {
      logger.info("need to authenticate")
      for {
        possibleSession: Option[Session] <- sessionDao.find
        newAuthentication: Authentication <- possibleSession match {
          case Some(session) =>
            logger.info("attempting to recreate authentication from stored session")
            val recreate = for {
              auth <- sGClient.createAuthentication(session)
            } yield auth

            val result = recreate recoverWith {
              case e: FailedToVerifyNewAuthenticationException =>
                logger.error("failed to verify stored session, defaulting to using username and password", e)
                authenticateWithUsernameAndPassword(passwordProvider)
            }

            result map { r: Authentication =>
              logger.info("successfully restored authentication")
              r
            }

          case None =>
            authenticateWithUsernameAndPassword(passwordProvider)
        }
      } yield {
        _authentication = newAuthentication
        newAuthentication
      }
    } else {
      Future.successful(authentication)
    }
  }

  private def authenticateWithUsernameAndPassword(passwordProvider: PasswordProvider): Future[Authentication] = {
    for {
      (username, plainTextPassword) <- passwordProvider.usernamePassword()
      newAuthentication <- sGClient.authenticate(username, plainTextPassword)
      _ <- sessionDao.create(newAuthentication.session)
    } yield newAuthentication
  }

  override def reifySuicideGirl(sg: SuicideGirl)(implicit pc: PatienceConfig): Future[SuicideGirl] = {
    reifyModel(SuicideGirlFactory)(sg)
  }

  override def reifyHopeful(hf: Hopeful)(implicit pc: PatienceConfig): Future[Hopeful] = {
    reifyModel(HopefulFactory)(hf)
  }

  override def reify(m: Model)(implicit pc: PatienceConfig): Future[Model] = {
    m match {
      case sg: SuicideGirl => reifyModel(SuicideGirlFactory)(sg)
      case hf: Hopeful => reifyModel(HopefulFactory)(hf)
    }

  }

  private def gatherAllPhotosFromSetPage(photoSetPageUri: URL): Future[List[Photo]] = {
    for {
      photoSetPageHTML <- sGClient.getPage(photoSetPageUri)
      photos <- Future fromTry {
        SGContentParser.parsePhotos(photoSetPageHTML).recoverWith {
          case NonFatal(_) => Failure(DidNotFindAnyPhotoLinksOnSetPageException(photoSetPageUri))
        }
      }
    } yield photos
  }

  private def reifyModel[T <: Model](mf: ModelFactory[T])(model: T)(implicit pc: PatienceConfig): Future[T] = {
    logger.info(s"SGReifier --> reifying: ${mf.name} ${model.name.name}. Expecting ${model.photoSets.length} sets")
    for {
      reifiedPhotoSets <- Future.serialize(model.photoSets) { photoSet =>
        pc.throttleQuarterAfter {
          for {
            photos <- this.gatherAllPhotosFromSetPage(photoSet.url) recoverWith {
              case e: DidNotFindAnyPhotoLinksOnSetPageException =>
                logger.error(s"SGReifier --> reifying: ${photoSet.url} has no photos. `${mf.name} ${model.name.name}`")
                Future.successful(Nil)
              case e: Throwable =>
                logger.error(s"SGReifier --> reifying: ${photoSet.url} failed to get parsed somehow. WTF?. `${mf.name} ${model.name.name}`", e)
                Future.successful(Nil)
            }
          } yield {
            logger.info(s"SGReifier --> reified: ${mf.name} ${model.name.name} photoset: ${photoSet.url}")
            photoSet.copy(photos = photos)
          }
        }
      }
    } yield {
      logger.info(s"reified ${mf.name} ${model.name.name}. Found ${reifiedPhotoSets.length} photo sets.")
      mf.apply(
        photoSetURL = model.photoSetURL,
        name = model.name,
        photoSets = reifiedPhotoSets
      )
    }
  }

  override def authentication: Authentication = _authentication
}
