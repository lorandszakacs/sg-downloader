package com.lorandszakacs.util.mongodb

import com.lorandszakacs.util.effects._
import com.typesafe.config.Config
import com.lorandszakacs.util.logger._
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 14 Jul 2017
  *
  */
final case class Database(uri: String, dbName: String, config: Option[Config] = None)(
  implicit
  private val dbIOScheduler: DBIOScheduler,
  private val futureLift:    FutureLift[IO],
) {

  implicit private val logger: Logger[IO] = Logger.getLogger[IO]

  def collection(colName: String): IO[BSONCollection] = databaseIO.map(_.apply(colName))

  private lazy val mongoDriverIO: IO[MongoDriver] = IO(
    new MongoDriver(config = config, classLoader = None),
  )

  private lazy val mongoConnectionIO: IO[MongoConnection] = {
    for {
      parsedURI   <- IO.fromTry(MongoConnection.parseURI(uri))
      mongoDriver <- mongoDriverIO
      connection  <- IO.fromTry(mongoDriver.connection(parsedURI, strictUri = true))
    } yield connection
  }

  private lazy val databaseIO: IO[DefaultDB] = {
    for {
      connection <- mongoConnectionIO
      db         <- Database.getDatabase(connection)(dbName)
    } yield db
  }

  def drop(): IO[Unit] = {
    for {
      db <- databaseIO
      _  <- logger.info(s"attempting to drop database: ${db.name}")
      _  <- db.drop().purifyIn[IO]
      _  <- logger.info(s"dropped database: ${db.name}")
    } yield ()
  }

  def shutdown(): IO[Unit] = {
    for {
      _      <- logger.info("attempting to close _mongoDriver.close(...)")
      driver <- mongoDriverIO
      _      <- IO(driver.close(1 minute))
      _      <- driver.system.terminate().purifyIn[IO]
      _      <- logger.info("terminated -- _mongoDriver.system.terminate()")
    } yield ()
  }
}

object Database {

  private[mongodb] def getDatabase(mongoConnection: MongoConnection)(name: String)(
    implicit
    sch:        DBIOScheduler,
    futureLift: FutureLift[IO],
  ): IO[DefaultDB] = {
    mongoConnection.database(name).purifyIn[IO].adaptError {
      case NonFatal(e) => new IllegalStateException(s"Failed to initialize Mongo database. Because: ${e.getMessage}", e)
    }
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
