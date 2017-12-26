package com.lorandszakacs.sg.reifier.impl

import java.net.URL

import com.lorandszakacs.sg.contentparser.SGContentParser
import com.lorandszakacs.sg.http._
import com.lorandszakacs.sg.model.M.{HFFactory, MFactory, SGFactory}
import com.lorandszakacs.sg.model.{HF, M, Photo, SG}
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
  private val sGClient:   SGClient,
  private val sessionDao: SessionDaoImpl
)(implicit val ec:        ExecutionContext)
    extends SGReifier with SGURLBuilder with StrictLogging {

  private[this] implicit var _authentication: Authentication = DefaultSGAuthentication

  override def authenticateIfNeeded()(implicit passwordProvider: PasswordProvider): Future[Authentication] = {
    if (authentication.needsRefresh) {
      logger.info("need to authenticate")
      for {
        possibleSession <- sessionDao.find
        newAuth <- possibleSession match {
                    case Some(session) =>
                      logger.info("attempting to recreate authentication from stored session")
                      val recreate = for {
                        auth <- sGClient.createAuthentication(session)
                      } yield auth

                      val result = recreate recoverWith {
                        case e: FailedToVerifyNewAuthenticationException =>
                          logger.error(
                            "failed to verify stored session, defaulting to using username and password",
                            e
                          )
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
        _authentication = newAuth
        newAuth
      }
    }
    else {
      Future.successful(authentication)
    }
  }

  private def authenticateWithUsernameAndPassword(passwordProvider: PasswordProvider): Future[Authentication] = {
    for {
      (username, plainTextPassword) <- passwordProvider.usernamePassword()
      newAuthentication             <- sGClient.authenticate(username, plainTextPassword)
      _                             <- sessionDao.create(newAuthentication.session)
    } yield newAuthentication
  }

  override def reifySG(sg: SG)(implicit pc: PatienceConfig): Future[SG] = {
    reifyM(SGFactory)(sg)
  }

  override def reifyHF(hf: HF)(implicit pc: PatienceConfig): Future[HF] = {
    reifyM(HFFactory)(hf)
  }

  override def reifyM(m: M)(implicit pc: PatienceConfig): Future[M] = {
    m match {
      case sg: SG => reifyM(SGFactory)(sg)
      case hf: HF => reifyM(HFFactory)(hf)
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

  private def reifyM[T <: M](mf: MFactory[T])(m: T)(implicit pc: PatienceConfig): Future[T] = {
    logger.info(s"SGReifier --> reifying: ${mf.name} ${m.name.name}. Expecting ${m.photoSets.length} sets")
    for {
      reifiedPhotoSets <- Future.serialize(m.photoSets) { photoSet =>
                           pc.throttleQuarterAfter {
                             for {
                               photos <- this.gatherAllPhotosFromSetPage(photoSet.url) recoverWith {
                                          case _: DidNotFindAnyPhotoLinksOnSetPageException =>
                                            logger.error(
                                              s"SGReifier --> reifying: ${photoSet.url} has no photos. `${mf.name} ${m.name.name}`"
                                            )
                                            Future.successful(Nil)
                                          case e: Throwable =>
                                            logger.error(
                                              s"SGReifier --> reifying: ${photoSet.url} failed to get parsed somehow. WTF?. `${mf.name} ${m.name.name}`",
                                              e
                                            )
                                            Future.successful(Nil)
                                        }
                             } yield {
                               logger.info(
                                 s"SGReifier --> reified: ${mf.name} ${m.name.name} photoset: ${photoSet.url}"
                               )
                               photoSet.copy(photos = photos)
                             }
                           }
                         }
    } yield {
      logger.info(s"reified ${mf.name} ${m.name.name}. Found ${reifiedPhotoSets.length} photo sets.")
      mf.apply(
        photoSetURL = m.photoSetURL,
        name        = m.name,
        photoSets   = reifiedPhotoSets
      )
    }
  }

  override def authentication: Authentication = _authentication
}
