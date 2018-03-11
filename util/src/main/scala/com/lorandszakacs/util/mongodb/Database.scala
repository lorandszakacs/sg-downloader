package com.lorandszakacs.util.mongodb

import com.typesafe.scalalogging.StrictLogging
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import com.lorandszakacs.util.effects._
import cats.Eval
import monix.execution.atomic.AtomicInt

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

  def collection(colName: String): IO[BSONCollection] = databaseIO.map(_.apply(colName))

  private lazy val mongoDriverIO: IO[MongoDriver] = IO(new MongoDriver())

  private lazy val mongoConnectionIO: IO[MongoConnection] = for {
    parsedURI   <- IO.fromTry(MongoConnection.parseURI(uri))
    mongoDriver <- mongoDriverIO
    connection <- IO(mongoDriver.connection(parsedURI)).map { c =>
      logger.info(s"have connection — ${Database.connectionCounter.getAndAdd(1)} —: ${c.name} ")
      c
    }
  } yield connection

  private lazy val databaseIO: IO[DefaultDB] = {
    for {
      connection <- mongoConnectionIO
      db         <- Database.getDatabase(connection)(dbName)
    } yield db

  }

  @scala.deprecated("use only for testing", "now")
  def connections: IO[String] = {
    for {
      driver <- mongoDriverIO
      cs     <- IO.pure(driver.connections.toList)
    } yield cs.map(c => s"${c.supervisor} -> ${c.actorSystem} -> ${c.name}").mkString("\n\n")
  }

  def drop(): IO[Unit] = {
    for {
      db <- databaseIO
      _  <- IO(logger.info(s"attempting to drop database: ${db.name}"))
      _  <- db.drop().suspendInIO >> IO(logger.info(s"dropped database: ${db.name}"))
    } yield ()
  }

  def shutdown(): IO[Unit] = {
    for {
      _      <- IO(logger.info("attempting to close _mongoDriver.close(...)"))
      driver <- mongoDriverIO
      _      <- IO(driver.close(1 minute))
      _      <- driver.system.terminate().suspendInIO >> IO(logger.info("terminated -- _mongoDriver.system.terminate()"))
    } yield ()
  }
}

object Database {

  @scala.deprecated("use only to count stuff", "2018")
  val connectionCounter: AtomicInt = monix.execution.atomic.AtomicInt(0)

  private[mongodb] def getDatabase(mongoConnection: MongoConnection)(name: String)(
    implicit
    ec: ExecutionContext
  ): IO[DefaultDB] = {
    mongoConnection.database(name).suspendInIO.adaptError {
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
