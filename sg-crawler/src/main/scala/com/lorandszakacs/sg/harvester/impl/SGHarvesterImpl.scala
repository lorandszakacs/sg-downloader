package com.lorandszakacs.sg.harvester.impl

import com.lorandszakacs.sg.crawler.ModelAndPhotoSetCrawler
import com.lorandszakacs.sg.harvester.SGHarvester
import com.lorandszakacs.sg.model.{SuicideGirl, SGModelRepository}

import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
private[harvester] class SGHarvesterImpl(
  val modelCrawler: ModelAndPhotoSetCrawler,
  val modelRepo: SGModelRepository
)(implicit
  val ec: ExecutionContext
) extends SGHarvester {

  override def updateSGIndex(maxTime: FiniteDuration): Future[List[String]] = ???

  override def updateHopefulIndex(timeout: FiniteDuration = 1 hour): Future[List[String]] = ???

  override def gatherPhotoSetInformationForSGsInIndex(timeout: FiniteDuration = 1 hour): Future[List[SuicideGirl]] = ???

}
