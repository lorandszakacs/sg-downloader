package com.lorandszakacs.sg.reifier.impl

import com.lorandszakacs.sg.http.Session

import com.typesafe.scalalogging.StrictLogging
import com.lorandszakacs.util.effects._
import com.lorandszakacs.util.mongodb._
import com.lorandszakacs.util.time._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 20 Jul 2016
  *
  */
private[reifier] final class SessionDaoImpl(
  override protected val db:       Database
)(implicit override val scheduler: Scheduler)
    extends SingleDocumentMongoCollection[Session, String, BSONString] with StrictLogging {

  private[reifier] implicit val dateTimeHandler
    : BSONReader[BSONDateTime, DateTime] with BSONWriter[DateTime, BSONDateTime] with BSONHandler[
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

  override protected implicit val idHandler: BSONHandler[BSONString, String] = BSONStringHandler

  override protected val uniqueDocumentId: String = "sg-session"

  override protected def defaultEntity: Session = Session(
    username  = "temp",
    sessionID = "123",
    csrfToken = "456",
    expiresAt = DateTime.now()
  )

  override def collectionName: String = "sg_sessions"

  private def onInit(): Task[Unit] = {
    for {
      opt <- this.find
      _ <- opt.isEmpty.effectOnTrueTask {
        this.create {
          logger.info("creating default session info")
          this.defaultEntity
        }
      }
    } yield ()
  }

  onInit()
}
