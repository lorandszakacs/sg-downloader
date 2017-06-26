package com.lorandszakacs.sg.crawler

import akka.actor.ActorSystem
import com.lorandszakacs.sg.crawler.impl.{ModelAndPhotoSetCrawlerImpl, PhotoMediaLinksCrawlerImpl, SessionDaoImpl}
import com.lorandszakacs.sg.http.SGClientAssembly
import reactivemongo.api.DefaultDB

import com.lorandszakacs.util.future._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
trait PageCrawlerAssembly extends SGClientAssembly {
  def db: DefaultDB

  implicit def actorSystem: ActorSystem

  implicit def executionContext: ExecutionContext

  def modelAndSetCrawler: ModelAndPhotoSetCrawler = _modelAndSetCrawler

  def photoMediaLinksCrawler: PhotoMediaLinksCrawler = _photoCrawler

  private[crawler] def sessionDao: SessionDao = _sessionDao

  private[crawler] lazy val _sessionDao = new SessionDaoImpl(db)(executionContext)

  private[crawler] lazy val _modelAndSetCrawler = new ModelAndPhotoSetCrawlerImpl(suicideGirlsClient)

  private[crawler] lazy val _photoCrawler = new PhotoMediaLinksCrawlerImpl(suicideGirlsClient, sessionDao)

}
