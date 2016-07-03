package com.lorandszakacs.sg.model.impl

import com.lorandszakacs.sg.model.{HopefulIndex, SuicideGirlIndex, SuicideGirl, SGModelRepository}

import scala.concurrent.{Future, ExecutionContext}

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
private[model] class SGModelRepositoryImpl(
  val nameIndexDao: NameIndexDao,
  val suicideGirlsDao: SuicideGirlsDao,
  val hopefulsDao: HopefulsDao
)(implicit val ec: ExecutionContext) extends SGModelRepository {

  override def createOrUpdateSGIndex(index: SuicideGirlIndex): Future[Unit] = {
    nameIndexDao.createOrUpdateSuicideGirlsIndex(index.names)
  }

  override def createOrUpdateHopefulIndex(index: HopefulIndex): Future[Unit] = {
    nameIndexDao.createOrUpdateHopefulIndex(index.names)
  }

  override def writeSG(sg: SuicideGirl): Future[Unit] = ???

  override def findSG(sgName: String): Future[Option[SuicideGirl]] = ???

  override def findSGsWithNoPhotoLinks(): Future[Seq[SuicideGirl]] = ???

  override def writeOrUpdate(sg: SuicideGirl): Future[Unit] = ???

  override def updateSG(sg: SuicideGirl): Future[Unit] = ???
}
