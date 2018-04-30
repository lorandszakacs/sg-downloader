package com.lorandszakacs.sg.reifier.impl

import com.lorandszakacs.sg.http.Session
import com.lorandszakacs.util.effects._
import com.lorandszakacs.util.mongodb._
import com.lorandszakacs.util.time._
import org.iolog4s.Logger

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 20 Jul 2016
  *
  */
private[reifier] final class SessionDaoImpl(
  override protected val db: Database
)(
  implicit
  override val dbIOScheduler: DBIOScheduler
) extends SingleDocumentMongoCollection[Session, String, BSONString] {

  implicit private val logger: Logger[Task] = Logger.create[Task]

  implicit private[reifier] val dateTimeHandler: BSONReader[BSONDateTime, DateTime]
    with BSONWriter[DateTime, BSONDateTime] with BSONHandler[
      BSONDateTime,
      DateTime
    ] =
    new BSONHandler[BSONDateTime, DateTime] {
      override def read(bson: BSONDateTime): DateTime = {
        new DateTime(bson.value, DateTimeZone.UTC)
      }

      override def write(t: DateTime): BSONDateTime = BSONDateTime(t.getMillis)
    }

  override protected val objectHandler: BSONDocumentHandler[Session] = BSONMacros.handler[Session]

  implicit override protected val idHandler: BSONHandler[BSONString, String] = BSONStringHandler

  override protected val uniqueDocumentId: String = "sg-session"

  override protected def defaultEntity: Session = Session(
    username  = "temp",
    sessionID = "123",
    csrfToken = "456",
    expiresAt = DateTime.now()
  )

  override def collectionName: String = "sg_sessions"

  private[reifier] def init: Task[Unit] = {
    for {
      opt <- this.find
      _ <- opt.isEmpty.effectOnTrueTask {
            logger.info("creating default session info") >> this.create(this.defaultEntity)
          }
    } yield ()
  }
}
