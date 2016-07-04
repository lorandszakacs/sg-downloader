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

  override def reindexSGs(names: List[ModelName]): Future[Unit] = {
    indexDao.reindexSGs(names)
  }

  override def reindexHopefuls(names: List[ModelName]): Future[Unit] = {
    indexDao.reindexHopefuls(names)
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
      _ <- indexDao.updateHopefulIndex(newHopefulIndex)
      _ <- indexDao.updateSGIndex(newSGIndex)

    } yield ()
  }

  override def createOrUpdateLastProcessed(l: LastProcessedMarker): Future[Unit] = {
    indexDao.createOrUpdateLastProcessedStatus(l)
  }

  override def lastProcessedIndex: Future[Option[LastProcessedMarker]] = {
    indexDao.lastProcessedStatus
  }

}
