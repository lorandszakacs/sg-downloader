package com.lorandszakacs.util.mongodb

import com.typesafe.scalalogging.StrictLogging
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import com.lorandszakacs.util.future._

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Try

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 14 Jul 2017
  *
  */
final case class Database(
  uri: String,
  dbName: String
)(implicit ec: ExecutionContext) extends StrictLogging {

  def value(): DefaultDB = _db

  def apply(colName: String): BSONCollection = this.value().apply(colName)

  private lazy val _mongoDriver: MongoDriver = new MongoDriver()
  private lazy val _mongoConnection: MongoConnection = _mongoDriver.connection(MongoConnection.parseURI(uri).get)
  private lazy val _dataBase: Try[DefaultDB] = {
    Try(Database.getDatabase(_mongoDriver, _mongoConnection)(dbName).await())
  }
  private lazy val _db = _dataBase.get

  def shutdown(): Future[Unit] = {
    for {
      _ <- Future fromTry Try {
        logger.info("attempting to close _mongoDriver.close(...)")
        _mongoDriver.close(1 minute)
      }
      _ <- _mongoDriver.system.terminate() map { _ =>
        logger.info("terminated -- _mongoDriver.system.terminate()")
      }
    } yield ()
  }
}

object Database {
  private[mongodb] def getDatabase(_mongoDriver: MongoDriver, _mongoConnection: MongoConnection)(name: String)
    (implicit ec: ExecutionContext): Future[DefaultDB] = {
    val future = {
      _mongoConnection.database(name)
    } recover {
      case e: Throwable =>
        throw new IllegalStateException(s"Failed to initialize Mongo database. Because: ${e.getMessage}", e)
    }
    future
  }
}
