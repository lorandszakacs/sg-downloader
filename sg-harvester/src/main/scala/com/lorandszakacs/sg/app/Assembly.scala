package com.lorandszakacs.sg.app

import java.util.concurrent.Executors

import akka.actor.ActorSystem
import com.lorandszakacs.sg.downloader.SGDownloaderAssembly
import com.lorandszakacs.sg.exporter.SGExporterAssembly
import com.lorandszakacs.sg.indexer.IndexerAssembly
import com.lorandszakacs.sg.model.SGRepoAssembly
import com.lorandszakacs.sg.reifier.ReifierAssembly
import com.lorandszakacs.util.future._
import com.lorandszakacs.util.mongodb.Database
import com.typesafe.scalalogging.StrictLogging

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 01 Jul 2017
  *
  */
class Assembly
    extends SGExporterAssembly with SGRepoAssembly with IndexerAssembly with ReifierAssembly with SGDownloaderAssembly
    with StrictLogging {

  override implicit lazy val db: Database = new Database(
    uri    = """mongodb://localhost:27016""",
    dbName = "sgs_repo"
  )

  override implicit lazy val actorSystem: ActorSystem = ActorSystem("sg-app")

  override implicit lazy val executionContext: ExecutionContext =
    ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  def shutdown(): Future[Unit] = {
    logger.info("attempting to shutdown and close all resources")
    for {
      _ <- db.shutdown() map { _ =>
            logger.info("terminated -- database.shutdown()")
          }
      _ <- actorSystem.terminate() map { _ =>
            logger.info("terminated -- actorSystem.terminate()")
          }
    } yield {
      logger.info("terminated -- completed assembly.shutdown()")
    }
  }

}
