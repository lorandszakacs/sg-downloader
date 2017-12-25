package com.lorandszakacs.sg.reifier.impl

import com.lorandszakacs.sg.http.Session

import com.typesafe.scalalogging.StrictLogging
import com.lorandszakacs.util.future._
import com.lorandszakacs.util.mongodb._
import com.lorandszakacs.util.time._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 20 Jul 2016
  *
  */
private[reifier] final class SessionDaoImpl(
  override protected val db:              Database
)(implicit override val executionContext: ExecutionContext)
    extends SingleDocumentMongoCollection[Session, String, BSONString] with StrictLogging {

  private implicit val dateTimeHandler
    : BSONReader[BSONDateTime, DateTime] with BSONWriter[DateTime, BSONDateTime] with BSONHandler[BSONDateTime,
                                                                                                  DateTime] =
    new BSONReader[BSONDateTime, DateTime] with BSONWriter[DateTime, BSONDateTime]
    with BSONHandler[BSONDateTime, DateTime] {
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

  private def onInit(): Unit = {
    for {
      opt <- this.find
      _ <- when(opt.isEmpty) execute this.create {
            logger.info("creating default session info")
            this.defaultEntity
          }
    } yield ()
  }

  onInit()
}
