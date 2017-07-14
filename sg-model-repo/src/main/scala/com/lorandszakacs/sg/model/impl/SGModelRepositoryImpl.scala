package com.lorandszakacs.sg.model.impl

import com.lorandszakacs.sg.model._
import com.typesafe.scalalogging.StrictLogging
import org.joda.time.LocalDate
import com.lorandszakacs.util.future._
import com.lorandszakacs.util.list._
import com.lorandszakacs.util.mongodb.Database
import com.lorandszakacs.util.time._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
private[model] class SGModelRepositoryImpl(
  val db: Database
)(implicit val ec: ExecutionContext) extends SGModelRepository with StrictLogging {

  private val sgsRepo = new RepoSuicideGirls(db)
  private val sgiRepo = new RepoSuicideGirlIndex(db)
  private val hfsRepo = new RepoHopefuls(db)
  private val hfiRepo = new RepoHopefulIndex(db)
  private val lpmRepo = new RepoLastProcessedMarker(db)

  override def modelsWithZeroPhotoSets: Future[Models] = {
    for {
      sgs <- sgsRepo.findWithZeroSets
      hopefuls <- hfsRepo.findWithZeroSets
    } yield (sgs, hopefuls).group
  }

  override def reindexSGs(names: List[ModelName]): Future[Unit] = {
    sgiRepo.rewriteIndex(names)
  }

  override def reindexHopefuls(names: List[ModelName]): Future[Unit] = {
    hfiRepo.rewriteIndex(names)
  }

  override def markAsIndexed(newHopefuls: List[Hopeful], newSGs: List[SuicideGirl]): Future[Unit] = {
    markAsIndexedForNames(
      newHopefuls = newHopefuls.map(_.name),
      newSGs = newSGs.map(_.name)
    )
  }

  override def markAsIndexedForNames(newHopefuls: List[ModelName], newSGs: List[ModelName]): Future[Unit] = {
    /**
      *
      * @return
      * hopefuls that became SGS
      */
    def updateHopeful(newHopefuls: List[ModelName]): Future[List[ModelName]] = {
      if (newHopefuls.isEmpty) {
        Future.successful(Nil)
      } else {
        for {
          oldHopefuls <- hfiRepo.get
          hopefulsThatBecameSGS: List[ModelName] = oldHopefuls.names.filter { ohn =>
            newSGs.contains(ohn.stripUnderscore)
          }
          //we remove all the hopefuls that became SGs from the index
          newHopefulIndex = oldHopefuls.copy(
            names = (oldHopefuls.names ++ newHopefuls).filterNot(n => hopefulsThatBecameSGS.contains(n.stripUnderscore)),
            needsReindexing = oldHopefuls.needsReindexing.diff(newHopefuls).filterNot(n => hopefulsThatBecameSGS.contains(n.stripUnderscore))
          )
          _ <- hfiRepo.createOrUpdate(newHopefulIndex)
          _ <- Future.traverse(hopefulsThatBecameSGS) { hopefulName =>
            hfsRepo.remove(hopefulName)
          }
        } yield hopefulsThatBecameSGS
      }
    }

    def updateSGs(newSGs: List[ModelName]): Future[Unit] = {
      if (newSGs.isEmpty) {
        Future.unit
      } else {
        for {
          oldSGs <- sgiRepo.get
          newSGIndex = oldSGs.copy(
            names = oldSGs.names ++ newSGs,
            needsReindexing = oldSGs.needsReindexing.diff(newSGs)
          )
          _ <- sgiRepo.createOrUpdate(newSGIndex)
        } yield ()
      }

    }

    for {
      hopefulsThatBecameSGs <- updateHopeful(newHopefuls)
      _ <- updateSGs(newSGs)
    } yield {
      logger.info(s"new SGs: ${newSGs.stringify}")
      logger.info(s"new Hopefuls: ${newHopefuls.stringify}")
      logger.info(s"hopefuls that became SGs: ${hopefulsThatBecameSGs.stringify}")
    }
  }

  override def createOrUpdateLastProcessed(l: LastProcessedMarker): Future[Unit] = {
    lpmRepo.createOrUpdate(l)
  }

  override def lastProcessedIndex: Future[Option[LastProcessedMarker]] = {
    lpmRepo.find
  }

  def completeModelIndex: Future[CompleteModelIndex] = {
    for {
      hs: HopefulIndex <- hfiRepo.get
      gs: SuicideGirlIndex <- sgiRepo.get
    } yield {
      val normalizedNames = (hs.names ++ gs.names).distinct.sorted
      val normalizedReindexing = (hs.needsReindexing ++ gs.needsReindexing).distinct.sorted
      CompleteModelIndex(
        names = normalizedNames,
        needsReindexing = normalizedReindexing,
        number = normalizedNames.length
      )
    }
  }

  override def createOrUpdateSGs(sgs: List[SuicideGirl]): Future[Unit] = {
    for {
      _ <- sgsRepo.createOrUpdate(sgs)
      _ <- this.markAsIndexed(newHopefuls = Nil, newSGs = sgs)
    } yield ()
  }

  override def createOrUpdateHopefuls(hopefuls: List[Hopeful]): Future[Unit] = {
    for {
      _ <- hfsRepo.createOrUpdate(hopefuls)
      _ <- this.markAsIndexed(newHopefuls = hopefuls, newSGs = Nil)
    } yield ()
  }

  private def groupModelsBetweenDays(start: LocalDate, end: LocalDate, models: List[Model]): List[(LocalDate, List[Model])] = {
    val days = TimeUtil.daysBetween(start, end)
    for {
      day <- days
      modelsForDay = models.filter(_.photoSets.exists(_.date == day))
    } yield (day, modelsForDay)
  }

  override def aggregateBetweenDays(start: LocalDate, end: LocalDate, models: List[Model]): Future[List[(LocalDate, List[Model])]] = {
    for {
      sgs <- sgsRepo.findBetweenDays(start, end)
      hopefuls <- hfsRepo.findBetweenDays(start, end)
      allFromDB: List[Model] = sgs ++ hopefuls
      all: List[Model] = allFromDB.addOrReplace(models)

      result = groupModelsBetweenDays(start, end, all)
    } yield result
  }

  override def find(modelName: ModelName): Future[Option[Model]] = {
    for {
      sg: Option[SuicideGirl] <- sgsRepo.find(modelName)
      hopeful: Option[Hopeful] <- hfsRepo.find(modelName)
    } yield if (sg.isDefined) sg else hopeful
  }

  override def find(modelNames: Seq[ModelName]): Future[List[Model]] = {
    for {
      sgs <- sgsRepo.findManyById(modelNames)
      hopefuls <- hfsRepo.findManyById(modelNames)
    } yield (sgs ++ hopefuls).sortBy(_.name)
  }

  override def findAll: Future[List[Model]] = {
    for {
      sgs <- sgsRepo.findAll
      hopefuls <- hfsRepo.findAll
    } yield (sgs ++ hopefuls).sortBy(_.name)
  }

}
