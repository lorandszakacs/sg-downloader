package com.lorandszakacs.sg.harvester.impl

import akka.http.scaladsl.model.StatusCodes
import com.lorandszakacs.sg.crawler.{DidNotFindAnyPhotoLinksOnSetPageException, ModelAndPhotoSetCrawler, PhotoMediaLinksCrawler}
import com.lorandszakacs.sg.harvester.SGHarvester
import com.lorandszakacs.sg.http.{FailedToGetPageException, PatienceConfig}
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
    logger.info(s"gatherNewestPhotosAndUpdateIndex $maxNrOfModels")
    for {
      lastProcessedOpt: Option[LastProcessedMarker] <- modelRepo.lastProcessedIndex
      _ = logger.info(s"the last processed set was: ${lastProcessedOpt.map(_.lastPhotoSetID)}")

      newModels: List[Model] <- modelCrawler.gatherNewestModelInformation(maxNrOfModels, lastProcessedOpt)
      _ = logger.info(s"gathered: ${newModels.map(_.name.name).mkString(",")}")

      _: Unit <- if (newModels.nonEmpty) {
        val gatheredNewerPhotoSet = lastProcessedOpt.exists { lp =>
          val newestPhotoSet: PhotoSet = newModels.head.photoSets.headOption.getOrElse(throw new AssertionError("... should have at least one set"))
          lp.lastPhotoSetID != newestPhotoSet.id
        }
        if (!gatheredNewerPhotoSet) {
          UnitFuture
        } else {
          val newIndex: LastProcessedMarker = modelCrawler.createLastProcessedIndex(newModels.head)
          logger.info(s"last processed marker is for: ${newIndex.model} @ ${newIndex.lastPhotoSetID}")
          modelRepo.createOrUpdateLastProcessed(newIndex)
        }
      } else {
        UnitFuture
      }
      (newSGS: List[SuicideGirl], newHopefuls: List[Hopeful]) = newModels.`SG|Hopeful`
      _ <- modelRepo.updateIndexes(newHopefuls = newHopefuls, newSGs = newSGS)
    } yield newModels
  }

  override def gatherAllDataForSuicideGirlsAndHopefulsFromScratch(usernameAndPassword: () => (String, String))(implicit pc: PatienceConfig): Future[List[Model]] = {
    for {
      _ <- photoCrawler.authenticateIfNeeded(usernameAndPassword)
      sgIndex <- modelRepo.suicideGirlIndex
      hopefulIndex <- modelRepo.hopefulIndex

      sgs: List[Try[SuicideGirl]] <- Future.serialize(sgIndex.names) { sgName =>
        harvestSuicideGirlAndUpdateIndex(sgName) map Success.apply recover {
          case NonFatal(e) =>
            logger.error(s"failed to harvest SG: ${sgName.name}", e)
            Failure(e)
        }
      }

      hopefuls: List[Try[Hopeful]] <- Future.serialize(hopefulIndex.names) { hopefulName =>
        harvestHopefulAndUpdateIndex(hopefulName) map Success.apply recover {
          case NonFatal(e) =>
            logger.error(s"failed to harvest hopeful: ${hopefulName.name}", e)
            Failure(e)
        }
      }
      result: List[Model] = sgs.filter(_.isSuccess).map(_.get) ++ hopefuls.filter(_.isSuccess).map(_.get)
    } yield result
  }

  override def authenticateIfNeeded(usernameAndPassword: () => (String, String)): Future[Unit] = {
    photoCrawler.authenticateIfNeeded(usernameAndPassword).map(_ => ())
  }

  private val ModelsKnownToHaveAMissingSet = List[ModelName](
    "aeta",
    "casanova",
    "casiopea",
    "damsel",
    "eliona",
    "kit",
    "kurupt",
    "vice"
  )

  override def gatherAllDataForSuicideGirlsAndHopefulsThatNeedIndexing(usernameAndPassword: () => (String, String), includeProblematic: Boolean)(implicit pc: PatienceConfig): Future[List[Model]] = {
    def adjust(names: List[ModelName]): List[ModelName] = {
      if (includeProblematic) names else names diff ModelsKnownToHaveAMissingSet
    }

    for {
      _ <- photoCrawler.authenticateIfNeeded(usernameAndPassword)
      sgIndex <- modelRepo.suicideGirlIndex
      hopefulIndex <- modelRepo.hopefulIndex

      sgs: List[Try[SuicideGirl]] <- Future.serialize(adjust(sgIndex.needsReindexing)) { sgName =>
        harvestSuicideGirlAndUpdateIndex(sgName) map Success.apply recover {
          case NonFatal(e) =>
            logger.error(s"failed to harvest SG: ${sgName.name}", e)
            Failure(e)
        }
      }

      hopefuls: List[Try[Hopeful]] <- Future.serialize(adjust(hopefulIndex.needsReindexing)) { hopefulName =>
        harvestHopefulAndUpdateIndex(hopefulName) map Success.apply recover {
          case NonFatal(e) =>
            logger.error(s"failed to harvest hopeful: ${hopefulName.name}", e)
            Failure(e)
        }
      }
      result: List[Model] = sgs.filter(_.isSuccess).map(_.get) ++ hopefuls.filter(_.isSuccess).map(_.get)
    } yield result
  }

  override def gatherDataAndUpdateModel(usernameAndPassword: () => (String, String), model: () => ModelName)(implicit pc: PatienceConfig): Future[Model] = {
    val name = model()
    for {
      _ <- photoCrawler.authenticateIfNeeded(usernameAndPassword)
      model: Model <- harvestSuicideGirlAndUpdateIndex(name) recoverWith {
        case NonFatal(e) => harvestHopefulAndUpdateIndex(name)
      }
    } yield model
  }

  override def cleanIndex()(implicit pc: PatienceConfig): Future[(List[ModelName], List[ModelName])] = {
    def notFoundModelName[T <: Model](m: T): PartialFunction[Throwable, Future[Option[ModelName]]] = {
      case e: FailedToGetPageException if e.response.status == StatusCodes.NotFound =>
        logger.error(s"${m.stringifyType} page @ ${m.photoSetURL.toExternalForm} was not found")
        Future.successful(None)
      case NonFatal(e) =>
        logger.error(s"${m.stringifyType} page @ ${m.photoSetURL.toExternalForm} encountered unknown error: ${e.getMessage}", e)
        Future.successful(None)

    }

    for {
      (sgsThatMightNeedCleaning, hopefulsThatMightNeedCleaning) <- modelRepo.modelsWithZeroPhotoSets
      _ = {
        logger.info(s"Total SGs that might needs cleaning up: ${sgsThatMightNeedCleaning.length}: ${sgsThatMightNeedCleaning.map(_.name.name).mkString(",")}")
        logger.info(s"Total Hopefuls that might needs cleaning up: ${hopefulsThatMightNeedCleaning.length}: ${hopefulsThatMightNeedCleaning.map(_.name.name).mkString(",")}")
      }

      sgs: List[ModelName] <- Future.serialize(sgsThatMightNeedCleaning) { sg =>
        pc.throttleThread()
        val eventualNameToRemove = for {
          suicideGirl: SuicideGirl <- modelCrawler.gatherPhotoSetInformationForModel(SuicideGirlFactory)(sg.name)
        } yield if (suicideGirl.photoSets.isEmpty) Option(suicideGirl.name) else None

        eventualNameToRemove recoverWith notFoundModelName(sg)
      } map (_.flatten.toList)


      hopefuls: List[ModelName] <- Future.serialize(hopefulsThatMightNeedCleaning) { hopeful =>
        pc.throttleThread()
        val eventualNameToRemove = for {
          hopeful: Hopeful <- modelCrawler.gatherPhotoSetInformationForModel(HopefulFactory)(hopeful.name)
        } yield if (hopeful.photoSets.isEmpty) Option(hopeful.name) else None

        eventualNameToRemove recoverWith notFoundModelName(hopeful)
      } map (_.flatten.toList)

      _ <- modelRepo.cleanUpModels(sgs = sgs, hopefuls = hopefuls)

    } yield (sgs, hopefuls)
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
      fullPhotoSets: List[PhotoSet] <- Future.traverse(modelWithNoPhotos.photoSets) { ph: PhotoSet =>
        for {
          photos <- photoCrawler.gatherAllPhotosFromSetPage(ph.url) recoverWith {
            case e: DidNotFindAnyPhotoLinksOnSetPageException =>
              logger.error(s"${ph.url} has no photos. `${mf.name} ${modelName.name}`")
              Future.successful(Nil)
            case e: Throwable =>
              logger.error(s"${ph.url} failed to get parsed somehow. WTF?. `${mf.name} ${modelName.name}`")
              Future.successful(Nil)
          }
        } yield {
          pc.throttleThread()
          ph.copy(photos = photos)
        }
      }
    } yield {
      pc.throttleThread()
      val result = modelWithNoPhotos.updatePhotoSets(fullPhotoSets)
      logger.info(s"harvested ${mf.name}: ${modelName.name}. #photoSets: ${result.photoSets.length} #photos: ${result.numberOfPhotos}")
      result
    }
  }
}
