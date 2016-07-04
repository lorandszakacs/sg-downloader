package com.lorandszakacs.sg.model.impl

import com.lorandszakacs.sg.model._

import scala.concurrent.{Future, ExecutionContext}

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
private[model] class SGModelRepositoryImpl(
  val indexDao: IndexDao,
  val suicideGirlsDao: SuicideGirlsDao,
  val hopefulsDao: HopefulsDao
)(implicit val ec: ExecutionContext) extends SGModelRepository {

  override def createOrUpdateSGIndex(index: SuicideGirlIndex): Future[Unit] = {
    indexDao.createOrUpdateSuicideGirlsIndex(index.names)
  }

  override def createOrUpdateHopefulIndex(index: HopefulIndex): Future[Unit] = {
    indexDao.createOrUpdateHopefulIndex(index.names)
  }

  override def createOrUpdateLastProcessed(l: LastProcessedIndex): Future[Unit] = {
    indexDao.createOrUpdateLastProcessedStatus(l)
  }

}
