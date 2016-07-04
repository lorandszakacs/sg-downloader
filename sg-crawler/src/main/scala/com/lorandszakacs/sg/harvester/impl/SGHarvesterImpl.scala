package com.lorandszakacs.sg.harvester.impl

import com.lorandszakacs.sg.crawler.ModelAndPhotoSetCrawler
import com.lorandszakacs.sg.harvester.SGHarvester
import com.lorandszakacs.sg.http.PatienceConfig
import com.lorandszakacs.sg.model._

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

  override def updateSGIndex(maxNrOfSGs: Int)(implicit pc: PatienceConfig): Future[List[ModelName]] = {
    for {
      names <- modelCrawler.gatherSGNames(maxNrOfSGs)
      _ <- modelRepo.createOrUpdateSGIndex {
        SuicideGirlIndex(
          names = names,
          number = names.length
        )
      }
    } yield names
  }

  override def updateHopefulIndex(maxNrOfHopefuls: Int)(implicit pc: PatienceConfig): Future[List[ModelName]] = {
    for {
      names <- modelCrawler.gatherHopefulNames(maxNrOfHopefuls)
      _ <- modelRepo.createOrUpdateHopefulIndex {
        HopefulIndex(
          names = names,
          number = names.length
        )
      }
    } yield names
  }

  override def gatherNewestPhotosAndUpdateIndex(maxNrOfSets: Int)(implicit pc: PatienceConfig): Future[List[Model]] = {
    modelCrawler.gatherNewestSets(maxNrOfSets)
    ???
  }

  override def gatherPhotoSetInformationForSGsInIndex(implicit pc: PatienceConfig): Future[List[SuicideGirl]] = ???

}
