package com.lorandszakacs.sg.harvester.impl

import com.lorandszakacs.sg.crawler.{DidNotFindAnyPhotoLinksOnSetPageException, ModelAndPhotoSetCrawler, PhotoMediaLinksCrawler}
import com.lorandszakacs.sg.harvester.SGHarvester
import com.lorandszakacs.sg.http.PatienceConfig
import com.lorandszakacs.sg.model.Model.{HopefulFactory, ModelFactory, SuicideGirlFactory}
import com.lorandszakacs.sg.model._
import com.lorandszakacs.util.monads.future.FutureUtil._
import com.typesafe.scalalogging.StrictLogging

import scala.language.postfixOps
import scala.util._
import scala.util.control.NonFatal

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
private[harvester] class SGHarvesterImpl(
  val modelCrawler: ModelAndPhotoSetCrawler,
  val photoCrawler: PhotoMediaLinksCrawler,
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
          val newestPhotoSet: PhotoSet = newModels.head.photoSets.headOption.getOrElse(throw new AssertionError("... should have at least one set"))
          lp.lastPhotoSetID != newestPhotoSet.id
        }
        if (!gatheredNewerPhotoSet) {
          UnitFuture
        } else {
          val newIndex: LastProcessedMarker = modelCrawler.createLastProcessedIndex(newModels.head)
          modelRepo.createOrUpdateLastProcessed(newIndex)
        }
      } else {
        UnitFuture
      }
      (newSGS: List[SuicideGirl], newHopefuls: List[Hopeful]) = newModels.`SG|Hopeful`
      _ <- modelRepo.updateIndexes(newHopefuls = newHopefuls, newSGs = newSGS)
    } yield newModels
  }


  override def gatherAllDataForSuicideGirlsAndHopefulsThatNeedIndexing(username: String, password: String)(implicit pc: PatienceConfig): Future[List[Model]] = {
    for {
      _ <- photoCrawler.authenticateIfNeeded(username, password)
      sgIndex <- modelRepo.suicideGirlIndex
      hopefulIndex <- modelRepo.hopefulIndex

      sgs: List[Try[SuicideGirl]] <- Future.serialize(sgIndex.needsReindexing) { sgName =>
        harvestSuicideGirlAndUpdateIndex(sgName) map Success.apply recover {
          case NonFatal(e) =>
            logger.error(s"failed to harvest SG: ${sgName.name}", e)
            Failure(e)
        }
      }

      hopefuls: List[Try[Hopeful]] <- Future.serialize(hopefulIndex.needsReindexing) { hopefulName =>
        harvestHopefulAndUpdateIndex(hopefulName) map Success.apply recover {
          case NonFatal(e) =>
            logger.error(s"failed to harvest hopeful: ${hopefulName.name}", e)
            Failure(e)
        }
      }
      result: List[Model] = sgs.filter(_.isSuccess).map(_.get) ++ hopefuls.filter(_.isSuccess).map(_.get)
    } yield result
  }

  private def harvestSuicideGirlAndUpdateIndex(sgName: ModelName)(implicit pc: PatienceConfig): Future[SuicideGirl] = {
    for {
      sg <- harvestModel(SuicideGirlFactory, sgName)
      _ <- modelRepo.createOrUpdateSG(sg)
    } yield sg
  }

  private def harvestHopefulAndUpdateIndex(hopefulName: ModelName)(implicit pc: PatienceConfig): Future[Hopeful] = {
    for {
      hopeful <- harvestModel(HopefulFactory, hopefulName)
      _ <- modelRepo.createOrUpdateHopeful(hopeful)
    } yield hopeful
  }

  /**
    *
    * Assumes that [[photoCrawler.authentication]] is valid in order to access specific
    * page information.
    *
    * @return
    * [[Model]] with complete information as it is available on the live website
    *
    */
  private def harvestModel[T <: Model with ModelUpdater[T]](mf: ModelFactory[T], modelName: ModelName)(implicit pc: PatienceConfig): Future[T] = {
    for {
      modelWithNoPhotos <- modelCrawler.gatherPhotoSetInformationForModel(mf)(modelName)
      fullPhotoSets: List[PhotoSet] <- Future.serialize(modelWithNoPhotos.photoSets) { ph: PhotoSet =>
        for {
          photos <- photoCrawler.gatherAllPhotosFromSetPage(ph.url) recoverWith {
            case e: DidNotFindAnyPhotoLinksOnSetPageException =>
              logger.error(s"${ph.url} has no photos. `${mf.name} ${modelName.name}`")
              Future.successful(Nil)
          }
        } yield {
          Thread.sleep(pc.throttle.toMillis)
          ph.copy(photos = photos)
        }
      }
    } yield {
      val result = modelWithNoPhotos.updatePhotoSets(fullPhotoSets)
      logger.info(s"harvested ${mf.name}: ${modelName.name}. #photoSets: ${result.photoSets.length} #photos: ${result.numberOfPhotos}")
      result
    }
  }
}
