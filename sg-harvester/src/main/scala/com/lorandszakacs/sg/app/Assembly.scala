package com.lorandszakacs.sg.app

import com.lorandszakacs.util.effects._
import com.lorandszakacs.util.mongodb._

import com.lorandszakacs.sg.downloader.SGDownloaderAssembly
import com.lorandszakacs.sg.exporter.SGExporterAssembly
import com.lorandszakacs.sg.indexer.IndexerAssembly
import com.lorandszakacs.sg.model.SGRepoAssembly
import com.lorandszakacs.sg.reifier.ReifierAssembly

import com.lorandszakacs.util.logger._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 01 Jul 2017
  *
  */
final class Assembly(
  implicit
  val contextShift:             ContextShift[IO],
  override val timer:           Timer[IO],
  override val dbIOScheduler:   DBIOScheduler,
  override val httpIOScheduler: HTTPIOScheduler,
  override val futureLift:      FutureLift[IO],
) extends SGExporterAssembly with SGRepoAssembly with IndexerAssembly with ReifierAssembly with SGDownloaderAssembly {

  implicit private val logger: Logger[IO] = Logger.getLogger[IO]

  override val concurrentEffect: ConcurrentEffect[IO] = IO.ioConcurrentEffect

  implicit override lazy val db: Database = new Database(
    uri    = """mongodb://localhost:27016""",
    dbName = "sgs_repo",
  )

  lazy val shutdownIO: IO[Unit] = {
    for {
      _ <- logger.info("attempting to shutdown and close all resources")
      _ <- logger.info("finished sgClient cleanup")
      _ <- db.shutdown() >> logger.info("terminated -- database.shutdown()")
      _ <- logger.info("terminated -- completed assembly.shutdown()")
    } yield ()
  }

  lazy val initIO: IO[Unit] = initReifierAssembly >> logger.info("initialized assembly")

}
