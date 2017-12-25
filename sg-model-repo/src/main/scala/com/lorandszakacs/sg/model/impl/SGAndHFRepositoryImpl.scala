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
private[model] class SGAndHFRepositoryImpl(
  val db: Database
)(implicit val ec: ExecutionContext) extends SGAndHFRepository with StrictLogging {

  private val sgsRepo = new RepoSGs(db)
  private val sgiRepo = new RepoSGIndex(db)
  private val hfsRepo = new RepoHFs(db)
  private val hfiRepo = new RepoHFIndex(db)
  private val lpmRepo = new RepoLastProcessedMarker(db)

  override def reindexSGs(names: List[Name]): Future[Unit] = {
    sgiRepo.rewriteIndex(names)
  }

  override def reindexHFs(names: List[Name]): Future[Unit] = {
    hfiRepo.rewriteIndex(names)
  }

  override def markAsIndexed(newHFs: List[HF], newSGs: List[SG]): Future[Unit] = {
    markAsIndexedForNames(
      newHFs = newHFs.map(_.name),
      newSGs = newSGs.map(_.name)
    )
  }

  override def markAsIndexedForNames(newHFs: List[Name], newSGs: List[Name]): Future[Unit] = {
    /**
      *
      * @return
      * HFs that became SGS
      */
    def updateHF(newHFs: List[Name]): Future[List[Name]] = {
      if (newHFs.isEmpty) {
        Future.successful(Nil)
      } else {
        for {
          ikdHFs <- hfiRepo.get
          hfsThatBecameSGS: List[Name] = ikdHFs.names.filter { ohn =>
            newSGs.contains(ohn.stripUnderscore)
          }
          //we remove all the HFs that became SGs from the index
          newHFIndex = ikdHFs.copy(
            names = (ikdHFs.names ++ newHFs).filterNot(n => hfsThatBecameSGS.contains(n.stripUnderscore)),
            needsReindexing = ikdHFs.needsReindexing.diff(newHFs).filterNot(n => hfsThatBecameSGS.contains(n.stripUnderscore))
          )
          _ <- hfiRepo.createOrUpdate(newHFIndex)
          _ <- Future.traverse(hfsThatBecameSGS) { hfName =>
            hfsRepo.remove(hfName)
          }
        } yield hfsThatBecameSGS
      }
    }

    def updateSGs(newSGs: List[Name]): Future[Unit] = {
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
      hfsThatBecameSGs <- updateHF(newHFs)
      _ <- updateSGs(newSGs)
    } yield {
      logger.info(s"new SGs: ${newSGs.stringify}")
      logger.info(s"new HFs: ${newHFs.stringify}")
      logger.info(s"HFs that became SGs: ${hfsThatBecameSGs.stringify}")
    }
  }

  override def createOrUpdateLastProcessed(l: LastProcessedMarker): Future[Unit] = {
    lpmRepo.createOrUpdate(l)
  }

  override def lastProcessedIndex: Future[Option[LastProcessedMarker]] = {
    lpmRepo.find
  }

  def completeIndex: Future[CompleteIndex] = {
    for {
      hfIndex: HFIndex <- hfiRepo.get
      sgIndex: SGIndex <- sgiRepo.get
    } yield {
      val normalizedNames = (hfIndex.names ++ sgIndex.names).distinct.sorted
      val normalizedReindexing = (hfIndex.needsReindexing ++ sgIndex.needsReindexing).distinct.sorted
      CompleteIndex(
        names = normalizedNames,
        needsReindexing = normalizedReindexing,
        number = normalizedNames.length
      )
    }
  }

  override def createOrUpdateSGs(sgs: List[SG]): Future[Unit] = {
    for {
      _ <- sgsRepo.createOrUpdate(sgs)
      _ <- this.markAsIndexed(newHFs = Nil, newSGs = sgs)
    } yield ()
  }

  override def createOrUpdateHFs(hfs: List[HF]): Future[Unit] = {
    for {
      _ <- hfsRepo.createOrUpdate(hfs)
      _ <- this.markAsIndexed(newHFs = hfs, newSGs = Nil)
    } yield ()
  }

  private def groupMsBetweenDays(start: LocalDate, end: LocalDate, models: List[M]): List[(LocalDate, List[M])] = {
    val days = TimeUtil.daysBetween(start, end)
    for {
      day <- days
      msForDay = models.filter(_.photoSets.exists(_.date == day))
    } yield (day, msForDay)
  }

  override def aggregateBetweenDays(start: LocalDate, end: LocalDate, ms: List[M]): Future[List[(LocalDate, List[M])]] = {
    for {
      sgs <- sgsRepo.findBetweenDays(start, end)
      hfs <- hfsRepo.findBetweenDays(start, end)
      allFromDB: List[M] = sgs ++ hfs
      all: List[M] = allFromDB.addOrReplace(ms)

      result = groupMsBetweenDays(start, end, all)
    } yield result
  }

  override def find(name: Name): Future[Option[M]] = {
    for {
      sg: Option[SG] <- sgsRepo.find(name)
      hf: Option[HF] <- hfsRepo.find(name)
    } yield if (sg.isDefined) sg else hf
  }

  override def find(names: Seq[Name]): Future[List[M]] = {
    for {
      sgs <- sgsRepo.findManyById(names)
      hfs <- hfsRepo.findManyById(names)
    } yield (sgs ++ hfs).sortBy(_.name)
  }

  override def findAll: Future[List[M]] = {
    for {
      sgs <- sgsRepo.findAll
      hfs <- hfsRepo.findAll
    } yield (sgs ++ hfs).sortBy(_.name)
  }

}
