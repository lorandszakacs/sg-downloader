package com.lorandszakacs.util.mongodb

import com.typesafe.scalalogging.StrictLogging
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import com.lorandszakacs.util.future._

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 14 Jul 2017
  *
  */
final case class Database(
  uri:         String,
  dbName:      String
)(implicit ec: ExecutionContext)
    extends StrictLogging {

  def apply(colName: String): IO[BSONCollection] = _dataBase.map(_.apply(colName))

  private lazy val _mongoDriver:     MongoDriver     = new MongoDriver()
  private lazy val _mongoConnection: MongoConnection = _mongoDriver.connection(MongoConnection.parseURI(uri).get)
  private lazy val _dataBase: IO[DefaultDB] = {
    Database.getDatabase(_mongoConnection)(dbName)
  }

  def drop(): IO[Unit] = {
    for {
      db <- _dataBase
      _  <- IO(logger.info(s"attempting to drop database: ${db.name}"))
      _  <- db.drop().suspendInIO >> IO(logger.info(s"dropped database: ${db.name}"))
    } yield ()
  }

  def shutdown(): IO[Unit] = {
    for {
      _ <- IO {
            logger.info("attempting to close _mongoDriver.close(...)")
            _mongoDriver.close(1 minute)
          }
      _ <- _mongoDriver.system.terminate().suspendInIO >> IO(
            logger.info("terminated -- _mongoDriver.system.terminate()")
          )
    } yield ()
  }
}

object Database {
  private[mongodb] def getDatabase(_mongoConnection: MongoConnection)(name: String)(
    implicit
    ec: ExecutionContext
  ): IO[DefaultDB] = {
    val io = {
      _mongoConnection.database(name).suspendInIO
    } recover {
      case e: Throwable =>
        throw new IllegalStateException(s"Failed to initialize Mongo database. Because: ${e.getMessage}", e)
    }
    io
  }

  /**
    * Convenience method used in testing. You should
    * pass it the text of the scala test. And ensure that
    * the text starts with a 3 digit string, e.g. 001
    */
  def testName(className: String, testText: String): String = {
    s"${className}_${testText.replace("should", "").replace(" ", "").take(3)}".trim()
  }
}
