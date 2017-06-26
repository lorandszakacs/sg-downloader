package com.lorandszakacs.sg.model.impl

import com.lorandszakacs.sg.model._
import com.typesafe.scalalogging.StrictLogging
import org.joda.time.LocalDate

import com.lorandszakacs.util.future._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
private[model] class SGModelRepositoryImpl(
  val indexDao: IndexDao,
  val suicideGirlsDao: SuicideGirlsDao,
  val hopefulsDao: HopefulsDao
)(implicit val ec: ExecutionContext) extends SGModelRepository with StrictLogging {

  override def modelsWithZeroPhotoSets: Future[(List[SuicideGirl], List[Hopeful])] = {
    for {
      sgs <- suicideGirlsDao.findWithZeroSets
      hopefuls <- hopefulsDao.findWithZeroSets
    } yield (sgs, hopefuls)
  }

  override def reindexSGs(names: List[ModelName]): Future[Unit] = {
    indexDao.rewriteSGIndex(names)
  }

  override def reindexHopefuls(names: List[ModelName]): Future[Unit] = {
    indexDao.rewriteHopefulsIndex(names)
  }

  override def updateIndexes(newHopefuls: List[Hopeful], newSGs: List[SuicideGirl]): Future[Unit] = {
    updateIndexesForNames(
      newHopefuls = newHopefuls.map(_.name),
      newSGs = newSGs.map(_.name)
    )
  }

  override def updateIndexesForNames(newHopefuls: List[ModelName], newSGs: List[ModelName]): Future[Unit] = {
    for {
      oldHopefuls <- indexDao.hopefulIndex
      oldSGs <- indexDao.suicideGirlsIndex
      hopefulsThatBecameSGS: List[ModelName] = oldHopefuls.names.filter { ohn =>
        newSGs.contains(ohn.stripUnderscore)
      }
      newSGIndex = oldSGs.copy(
        names = oldSGs.names ++ newSGs,
        needsReindexing = oldSGs.needsReindexing ++ newSGs
      )
      //we remove all the hopefuls that became SGs from the index
      newHopefulIndex = oldHopefuls.copy(
        names = (oldHopefuls.names ++ newHopefuls).filterNot(n => hopefulsThatBecameSGS.contains(n.stripUnderscore)),
        needsReindexing = (oldHopefuls.needsReindexing ++ newHopefuls).filterNot(n => hopefulsThatBecameSGS.contains(n.stripUnderscore))
      )
      _ <- indexDao.rewriteHopefulsIndex(newHopefulIndex)
      _ <- indexDao.rewriteSGIndex(newSGIndex)
      _ <- Future.traverse(hopefulsThatBecameSGS) { hopefulName =>
        hopefulsDao.delete(hopefulName)
      }
    } yield {
      logger.info(s"new SGs: ${newSGs.map(_.name).mkString(",")}")
      logger.info(s"new Hopefuls: ${newHopefuls.map(_.name).mkString(",")}")
      logger.info(s"hopefuls that became SGs:: ${hopefulsThatBecameSGS.map(_.name).mkString(",")}")
    }
  }

  override def cleanUpModels(sgs: List[ModelName], hopefuls: List[ModelName]): Future[Unit] = {
    for {
      _ <- indexDao.cleanUp(sgs, hopefuls)
      _ <- Future.traverse(sgs) { sg =>
        suicideGirlsDao.delete(sg.name)
      }

      _ <- Future.traverse(hopefuls) { hopeful =>
        hopefulsDao.delete(hopeful.name)
      }
    } yield ()
  }

  override def createOrUpdateLastProcessed(l: LastProcessedMarker): Future[Unit] = {
    indexDao.createOrUpdateLastProcessedStatus(l)
  }

  override def lastProcessedIndex: Future[Option[LastProcessedMarker]] = {
    indexDao.lastProcessedStatus
  }

  override def suicideGirlIndex: Future[SuicideGirlIndex] = {
    indexDao.suicideGirlsIndex
  }

  override def hopefulIndex: Future[HopefulIndex] = {
    indexDao.hopefulIndex
  }

  def completeModelIndex: Future[CompleteModelIndex] = {
    for {
      hs <- indexDao.hopefulIndex
      gs <- indexDao.suicideGirlsIndex
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

  override def createOrUpdateSG(sg: SuicideGirl): Future[Unit] = {
    for {
      _ <- suicideGirlsDao.createOrUpdate(sg)
      emptyPhotoSets = sg.photoSets.filter(_.photos.isEmpty)
      _ <- if (emptyPhotoSets.isEmpty) {
        indexDao.markSGAsIndexed(sg.name)
      } else {
        Future.successful(())
      }
    } yield ()
  }

  override def createOrUpdateHopeful(hopeful: Hopeful): Future[Unit] = {
    for {
      _ <- hopefulsDao.createOrUpdate(hopeful)
      emptyPhotoSets = hopeful.photoSets.filter(_.photos.isEmpty)
      _ <- if (emptyPhotoSets.isEmpty) {
        indexDao.markHopefulAsIndexed(hopeful.name)
      } else {
        Future.successful(())
      }
    } yield ()
  }

  override def aggregateBetweenDays(start: LocalDate, end: LocalDate): Future[List[(LocalDate, List[Model])]] = {
    for {
      sgs <- suicideGirlsDao.findBetweenDays(start, end)
      hopefuls <- hopefulsDao.findBetweenDays(start, end)

      all: List[Model] = sgs ++ hopefuls
      days = RepoTimeUtil.daysBetween(start, end)
      models = for {
        day <- days
        modelsForDay = all.filter(_.photoSets.exists(_.date == day))
      } yield (day, modelsForDay)
    } yield models
  }

  override def find(modelName: ModelName): Future[Option[Model]] = {
    for {
      sg: Option[SuicideGirl] <- suicideGirlsDao.find(modelName)
      hopeful: Option[Hopeful] <- hopefulsDao.find(modelName)
    } yield if (sg.isDefined) sg else hopeful
  }

  override def find(modelNames: Seq[ModelName]): Future[List[Model]] = {
    for {
      sgs <- suicideGirlsDao.find(modelNames)
      hopefuls <- hopefulsDao.find(modelNames)
    } yield (sgs ++ hopefuls).sortBy(_.name)
  }

  override def findAll: Future[List[Model]] = {
    for {
      sgs <- suicideGirlsDao.findAll
      hopefuls <- hopefulsDao.findAll
    } yield (sgs ++ hopefuls).sortBy(_.name)
  }

}
