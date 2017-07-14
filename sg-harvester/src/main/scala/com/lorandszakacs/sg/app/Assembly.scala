package com.lorandszakacs.sg.app

import java.util.concurrent.Executors

import akka.actor.ActorSystem
import com.lorandszakacs.sg.downloader.SGDownloaderAssembly
import com.lorandszakacs.sg.exporter.ModelExporterAssembly
import com.lorandszakacs.sg.indexer.IndexerAssembly
import com.lorandszakacs.sg.model.SGModelAssembly
import com.lorandszakacs.sg.reifier.ReifierAssembly
import com.lorandszakacs.util.future._
import com.typesafe.scalalogging.StrictLogging
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Try

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 01 Jul 2017
  *
  */
class Assembly extends ModelExporterAssembly with SGModelAssembly with IndexerAssembly with ReifierAssembly with SGDownloaderAssembly with StrictLogging {
  override implicit lazy val actorSystem: ActorSystem = ActorSystem("sg-app")

  override implicit lazy val executionContext: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  override lazy val db: DefaultDB = _dataBase.get

  private lazy val _mongoDriver: MongoDriver = new MongoDriver()
  private lazy val _dataBase: Try[DefaultDB] = {
    val future = {
      val _mongoConnection: MongoConnection = _mongoDriver.connection(MongoConnection.parseURI("""mongodb://localhost""").get)
      _mongoConnection.database("sgs_repo")
    } recover {
      case e: Throwable =>
        throw new IllegalStateException(s"Failed to initialize Mongo database. Because: ${e.getMessage}", e)
    }
    Try(future.await())
  }

  def shutdown(): Future[Unit] = {
    logger.info("attempting to shutdown and close all resources")
    for {
      _ <- Future fromTry Try {
        _mongoDriver.close(1 minute)
        logger.info("attempting to close _mongoDriver.close(...)")
      }
      _ <- _mongoDriver.system.terminate() map { _ =>
        logger.info("terminated -- _mongoDriver.system.terminate()")
      }

      _ <- actorSystem.terminate() map { _ =>
        logger.info("terminated -- actorSystem.terminate()")
      }

    } yield {
      logger.info("terminated -- completed assembly.shutdown()")
    }
  }

}
