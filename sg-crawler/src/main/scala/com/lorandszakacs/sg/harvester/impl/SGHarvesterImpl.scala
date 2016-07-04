package com.lorandszakacs.sg.harvester.impl

import com.lorandszakacs.sg.crawler.ModelAndPhotoSetCrawler
import com.lorandszakacs.sg.harvester.SGHarvester
import com.lorandszakacs.sg.http.PatienceConfig
import com.lorandszakacs.sg.model._
import com.typesafe.scalalogging.StrictLogging

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
) extends SGHarvester with StrictLogging {

  override def reindexSGNames(maxNrOfSGs: Int)(implicit pc: PatienceConfig): Future[List[ModelName]] = {
    for {
      names <- modelCrawler.gatherSGNames(maxNrOfSGs)
      _ <- modelRepo.reindexSGs(names)
    } yield names
  }

  override def reindexHopefulsNames(maxNrOfHopefuls: Int)(implicit pc: PatienceConfig): Future[List[ModelName]] = {
    for {
      names <- modelCrawler.gatherHopefulNames(maxNrOfHopefuls)
      _ <- modelRepo.reindexHopefuls(names)
    } yield names
  }

  override def reindexAll(maxNrOfReindexing: Int)(implicit pc: PatienceConfig): Future[List[ModelName]] = {
    for {
      newModels: List[Model] <- modelCrawler.gatherNewestModelInformation(1, None)
      newStatusMarker = modelCrawler.createLastProcessedIndex(newModels.head)
      _ <- modelRepo.createOrUpdateLastProcessed(newStatusMarker)

      _ = logger.info(s"created new status marker: ${newStatusMarker.lastPhotoSetID}")

      sgNames <- this.reindexSGNames(maxNrOfReindexing)
      _ = logger.info(s"finished reindexing: ${sgNames.length} SuicideGirls")
      remainingNr = maxNrOfReindexing - sgNames.length

      hopefulNames <- this.reindexHopefulsNames(remainingNr)
      _ = logger.info(s"finished reindexing: ${hopefulNames.length} Hopefuls")

    } yield sgNames ++ hopefulNames
  }

  override def gatherNewestPhotosAndUpdateIndex(maxNrOfModels: Int)(implicit pc: PatienceConfig): Future[List[Model]] = {
    for {
      lastProcessedOpt: Option[LastProcessedMarker] <- modelRepo.lastProcessedIndex

      newModels: List[Model] <- modelCrawler.gatherNewestModelInformation(maxNrOfModels, lastProcessedOpt)

      _: Unit <- if (newModels.nonEmpty) {
        val gatheredNewerPhotoSet = lastProcessedOpt.exists { lp =>
          val newestPhotoset: PhotoSet = newModels.head.photoSets.headOption.getOrElse(throw new AssertionError("... should have at least one set"))
          lp.lastPhotoSetID != newestPhotoset.id
        }
        if (!gatheredNewerPhotoSet) {
          Future.successful(())
        } else {
          val newIndex: LastProcessedMarker = modelCrawler.createLastProcessedIndex(newModels.head)
          modelRepo.createOrUpdateLastProcessed(newIndex)
        }
      } else {
        Future.successful(())
      }
      (newSGS: List[SuicideGirl], newHopefuls: List[Hopeful]) = newModels.`SG|Hopeful`
      _ <- modelRepo.updateIndexes(newHopefuls = newHopefuls, newSGs = newSGS)
    } yield newModels
  }
}
