package com.lorandszakacs.sg.crawler.impl

import com.lorandszakacs.sg.crawler.SessionDao
import com.lorandszakacs.sg.http.Session
import com.lorandszakacs.util.monads.future.FutureUtil._
import org.joda.time.{DateTime, DateTimeZone}
import reactivemongo.api.DB
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDateTime, BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONHandler, BSONReader, BSONWriter, Macros}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 20 Jul 2016
  *
  */
private[crawler] final class SessionDaoImpl(val db: DB)(implicit ec: ExecutionContext) extends SessionDao {
  protected lazy val collection: BSONCollection = db("sg_sessions")

  private val sessionId = "sg-session"
  private val _id = "_id"

  private val idBSON = BSONDocument(_id -> sessionId)

  private implicit val dateTimeHandler: BSONReader[BSONDateTime, DateTime] with BSONWriter[DateTime, BSONDateTime] with BSONHandler[BSONDateTime, DateTime] =
    new BSONReader[BSONDateTime, DateTime] with BSONWriter[DateTime, BSONDateTime] with BSONHandler[BSONDateTime, DateTime] {
      override def read(bson: BSONDateTime): DateTime = {
        new DateTime(bson.value, DateTimeZone.UTC)
      }

      override def write(t: DateTime): BSONDateTime = BSONDateTime(t.getMillis)
    }

  private implicit val sessionHandler: BSONDocumentReader[Session] with BSONDocumentWriter[Session] with BSONHandler[BSONDocument, Session] =
    new BSONDocumentReader[Session] with BSONDocumentWriter[Session] with BSONHandler[BSONDocument, Session] {
      val handler: BSONDocumentReader[Session] with BSONDocumentWriter[Session] with BSONHandler[BSONDocument, Session] = Macros.handler[Session]

      override def read(bson: BSONDocument): Session = handler.read(bson)

      override def write(t: Session): BSONDocument = {
        val b = handler.write(t)
        b ++ idBSON
      }
    }

  override def create(session: Session): Future[Unit] = {
    collection.update(idBSON, session, upsert = true) map UnitFunction
  }

  override def find(): Future[Option[Session]] = {
    collection.find(idBSON).one[Session]
  }

}
