package com.lorandszakacs.sg.app

import com.lorandszakacs.util.effects._

import akka.actor.ActorSystem
import com.lorandszakacs.sg.downloader.SGDownloaderAssembly
import com.lorandszakacs.sg.exporter.SGExporterAssembly
import com.lorandszakacs.sg.indexer.IndexerAssembly
import com.lorandszakacs.sg.model.SGRepoAssembly
import com.lorandszakacs.sg.reifier.ReifierAssembly
import com.lorandszakacs.util.mongodb.Database
import com.typesafe.scalalogging.StrictLogging

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 01 Jul 2017
  *
  */
final class Assembly(
  override val actorSystem: ActorSystem = ActorSystem("sg-app"),
  override val scheduler:   Scheduler   = Scheduler.io(name = "sg-app")
) extends SGExporterAssembly with SGRepoAssembly with IndexerAssembly with ReifierAssembly with SGDownloaderAssembly
    with StrictLogging {

  override implicit lazy val db: Database = new Database(
    uri    = """mongodb://localhost:27016""",
    dbName = "sgs_repo"
  )(scheduler)

  def shutdown(): Task[Unit] = {
    for {
      _ <- Task(logger.info("attempting to shutdown and close all resources"))
      _ <- db.shutdown() >> Task(logger.info("terminated -- database.shutdown()"))
//      _ <- actorSystem.terminate().suspendInTask >> Task(logger.info("terminated -- actorSystem.terminate()"))
      _ <- Task(logger.info("terminated -- completed assembly.shutdown()"))
    } yield ()
  }

}
