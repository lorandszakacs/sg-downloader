package com.lorandszakacs.sg.reifier.impl

import com.lorandszakacs.sg.http.Session
import com.lorandszakacs.sg.reifier.SessionDao
import com.lorandszakacs.util.future._
import com.typesafe.scalalogging.StrictLogging
import org.joda.time.{DateTime, DateTimeZone}
import com.lorandszakacs.util.mongodb.Imports._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 20 Jul 2016
  *
  */
private[reifier] final class SessionDaoImpl(val db: Database)(implicit ec: ExecutionContext) extends SessionDao with StrictLogging {
  protected lazy val collection: BSONCollection = db("sg_sessions")

  private val sessionId = "sg-session"
  private val _id = "_id"

  private val idBSON = BSONDocument(_id -> sessionId)

  private def onInit(): Unit = {
    for {
      opt <- this.find()
      _ <- when(opt.isEmpty) execute this.create {
        logger.info("creating default session info")
        Session(
          username = "default",
          sessionID = "default",
          csrfToken = "default",
          expiresAt = DateTime.now().plusDays(128)
        )
      }
    } yield ()
  }

  onInit()

  private implicit val dateTimeHandler: BSONReader[BSONDateTime, DateTime] with BSONWriter[DateTime, BSONDateTime] with BSONHandler[BSONDateTime, DateTime] =
    new BSONReader[BSONDateTime, DateTime] with BSONWriter[DateTime, BSONDateTime] with BSONHandler[BSONDateTime, DateTime] {
      override def read(bson: BSONDateTime): DateTime = {
        new DateTime(bson.value, DateTimeZone.UTC)
      }

      override def write(t: DateTime): BSONDateTime = BSONDateTime(t.getMillis)
    }

  private implicit val sessionHandler: BSONDocumentReader[Session] with BSONDocumentWriter[Session] with BSONHandler[BSONDocument, Session] =
    new BSONDocumentReader[Session] with BSONDocumentWriter[Session] with BSONHandler[BSONDocument, Session] {
      val handler: BSONDocumentReader[Session] with BSONDocumentWriter[Session] with BSONHandler[BSONDocument, Session] = BSONMacros.handler[Session]

      override def read(bson: BSONDocument): Session = handler.read(bson)

      override def write(t: Session): BSONDocument = {
        val b = handler.write(t)
        b ++ idBSON
      }
    }

  override def create(session: Session): Future[Unit] = {
    collection.update(idBSON, session, upsert = true) map `Any => Unit`
  }

  override def find(): Future[Option[Session]] = {
    collection.find(idBSON).one[Session]
  }

}
