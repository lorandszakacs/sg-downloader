package com.lorandszakacs.sg.reifier.impl

import com.lorandszakacs.util.effects._
import java.net.URL

import com.lorandszakacs.sg.contentparser.SGContentParser
import com.lorandszakacs.sg.http._
import com.lorandszakacs.sg.model.M._
import com.lorandszakacs.sg.model._
import com.lorandszakacs.sg.reifier._
import com.typesafe.scalalogging.StrictLogging

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 05 Jul 2016
  *
  */
private[reifier] class SGReifierImpl(
  private val sGClient:   SGClient,
  private val sessionDao: SessionDaoImpl
) extends SGReifier with SGURLBuilder with StrictLogging {

  private[this] implicit var _authentication: Authentication = DefaultSGAuthentication

  override def authenticateIfNeeded(): Task[Authentication] = {
    if (authentication.needsRefresh) {
      for {
        _               <- Task(logger.info("need to authenticate"))
        possibleSession <- sessionDao.find
        newAuth <- possibleSession match {
          case Some(session) =>
            val recreate = for {
              _    <- Task(logger.info("attempting to recreate authentication from stored session"))
              auth <- sGClient.createAuthentication(session)
            } yield auth
            recreate

          // val result = recreate recoverWith {
          //   case e: FailedToVerifyNewAuthenticationException =>
          //     logger.error(
          //       "failed to verify stored session, defaulting to using username and password",
          //       e
          //     )
          //     authenticateWithUsernameAndPassword(passwordProvider)
          // }
          //
          // result map { r: Authentication =>
          //   logger.info("successfully restored authentication")
          //   r
          // }

          case None => Task.raiseError(NoSessionFoundException)
        }
      } yield {
        _authentication = newAuth
        newAuth
      }
    }
    else {
      Task.pure(authentication)
    }
  }

  override def reifySG(sg: SG)(implicit pc: PatienceConfig): Task[SG] = {
    reifyM(SGFactory)(sg)
  }

  override def reifyHF(hf: HF)(implicit pc: PatienceConfig): Task[HF] = {
    reifyM(HFFactory)(hf)
  }

  override def reifyM(m: M)(implicit pc: PatienceConfig): Task[M] = {
    m match {
      case sg: SG => reifyM(SGFactory)(sg)
      case hf: HF => reifyM(HFFactory)(hf)
    }

  }

  private def gatherAllPhotosFromSetPage(photoSetPageUri: URL): Task[List[Photo]] = {
    for {
      photoSetPageHTML <- sGClient.getPage(photoSetPageUri)
      photos <- Task.fromTry(SGContentParser.parsePhotos(photoSetPageHTML)).recoverWith {
        case NonFatal(_) => Task.failThr(DidNotFindAnyPhotoLinksOnSetPageException(photoSetPageUri))
      }

    } yield photos
  }

  private def reifyM[T <: M](mf: MFactory[T])(m: T)(implicit pc: PatienceConfig): Task[T] = {
    def reifyPhotoSets(photoSet: PhotoSet): Task[PhotoSet] = {
      pc.throttleQuarterAfter {
        for {
          photos <- this.gatherAllPhotosFromSetPage(photoSet.url) recoverWith {
            case _: DidNotFindAnyPhotoLinksOnSetPageException =>
              Task(logger.error(s"SGReifier --> reifying: ${photoSet.url} has no photos. `${mf.name} ${m.name.name}`")) >>
                Task.pure(Nil)
            case e: Throwable =>
              Task(
                logger.error(
                  s"SGReifier --> reifying: ${photoSet.url} failed to get parsed somehow. WTF?. `${mf.name} ${m.name.name}`",
                  e
                )
              ) >> Task.pure(Nil)
          }
          _ <- Task(logger.info(s"SGReifier --> reified: ${mf.name} ${m.name.name} photoset: ${photoSet.url}"))
        } yield photoSet.copy(photos = photos)
      }
    }
    for {
      _      <- Task(logger.info(s"SGReifier --> reifying: ${mf.name} ${m.name.name}. Expecting ${m.photoSets.length} sets"))
      result <- Task.serialize(m.photoSets)(reifyPhotoSets)
      _      <- Task(logger.info(s"reified ${mf.name} ${m.name.name}. Found ${result.length} photo sets."))
    } yield
      mf.apply(
        photoSetURL = m.photoSetURL,
        name        = m.name,
        photoSets   = result
      )

  }

  override def authentication: Authentication = _authentication
}
