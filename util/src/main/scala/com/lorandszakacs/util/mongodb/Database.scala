package com.lorandszakacs.util.mongodb

import com.typesafe.scalalogging.StrictLogging
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import com.lorandszakacs.util.effects._

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 14 Jul 2017
  *
  */
final case class Database(
  uri:          String,
  dbName:       String
)(implicit sch: Scheduler)
    extends StrictLogging {

  def collection(colName: String): Task[BSONCollection] = databaseTask.map(_.apply(colName))

  private lazy val mongoDriverTask: Task[MongoDriver] = Task(new MongoDriver()).memoizeOnSuccess

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
      _  <- Task(logger.info(s"attempting to drop database: ${db.name}"))
      _  <- db.drop().suspendInTask >> Task(logger.info(s"dropped database: ${db.name}"))
    } yield ()
  }

  def shutdown(): Task[Unit] = {
    for {
      _      <- Task(logger.info("attempting to close _mongoDriver.close(...)"))
      driver <- mongoDriverTask
      _      <- Task(driver.close(1 minute))
      _      <- driver.system.terminate().suspendInTask >> Task(logger.info("terminated -- _mongoDriver.system.terminate()"))
    } yield ()
  }
}

object Database {

  private[mongodb] def getDatabase(mongoConnection: MongoConnection)(name: String)(
    implicit
    sch: Scheduler
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
