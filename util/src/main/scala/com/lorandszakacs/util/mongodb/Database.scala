package com.lorandszakacs.util.mongodb

import com.lorandszakacs.util.effects._
import com.typesafe.config.Config
import org.iolog4s.Logger
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
  dbIOScheduler: DBIOScheduler
) {

  implicit private val logger: Logger[Task] = Logger.create[Task]

  def collection(colName: String): Task[BSONCollection] = databaseTask.map(_.apply(colName))

  private lazy val mongoDriverTask: Task[MongoDriver] = Task(
    new MongoDriver(config = config, classLoader = None)
  ).memoizeOnSuccess

  private lazy val mongoConnectionTask: Task[MongoConnection] = {
    for {
      parsedURI   <- Task.fromTry(MongoConnection.parseURI(uri))
      mongoDriver <- mongoDriverTask
      connection  <- Task(mongoDriver.connection(parsedURI))
    } yield connection
  }.memoizeOnSuccess

  private lazy val databaseTask: Task[DefaultDB] = {
    for {
      connection <- mongoConnectionTask
      db         <- Database.getDatabase(connection)(dbName)
    } yield db
  }.memoizeOnSuccess

  def drop(): Task[Unit] = {
    for {
      db <- databaseTask
      _  <- logger.info(s"attempting to drop database: ${db.name}")
      _  <- db.drop().suspendInTask
      _  <- logger.info(s"dropped database: ${db.name}")
    } yield ()
  }

  def shutdown(): Task[Unit] = {
    for {
      _      <- logger.info("attempting to close _mongoDriver.close(...)")
      driver <- mongoDriverTask
      _      <- Task(driver.close(1 minute))
      _      <- driver.system.terminate().suspendInTask
      _      <- logger.info("terminated -- _mongoDriver.system.terminate()")
    } yield ()
  }
}

object Database {

  private[mongodb] def getDatabase(mongoConnection: MongoConnection)(name: String)(
    implicit
    sch: DBIOScheduler
  ): Task[DefaultDB] = {
    mongoConnection.database(name).suspendInTask.adaptError {
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
