package com.lorandszakacs.sg.crawler

import akka.actor.ActorSystem
import com.lorandszakacs.sg.crawler.impl.ModelAndPhotoSetCrawlerImpl
import com.lorandszakacs.sg.http.SGClientAssembly

import scala.concurrent.ExecutionContext

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
trait PageCrawlerAssembly extends SGClientAssembly {
  implicit def actorSystem: ActorSystem

  implicit def executionContext: ExecutionContext

  def modelAndSetCrawler: ModelAndPhotoSetCrawler = _modelAndSetCrawler

  private[crawler] lazy val _modelAndSetCrawler = new ModelAndPhotoSetCrawlerImpl(sgClientWithNoAuthentication)

}
