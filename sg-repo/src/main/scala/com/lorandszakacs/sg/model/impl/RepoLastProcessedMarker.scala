package com.lorandszakacs.sg.model.impl

import com.lorandszakacs.sg.model._
import com.lorandszakacs.util.time._
import com.lorandszakacs.util.effects._
import com.lorandszakacs.util.mongodb._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 14 Jul 2017
  *
  */
private[impl] class RepoLastProcessedMarker(override protected val db: Database)(
  implicit
  override val dbIOScheduler: DBIOScheduler,
) extends IndexSingleDocRepo[LastProcessedMarker] with SGRepoBSON {

  override protected def objectHandler: BSONDocumentHandler[LastProcessedMarker] =
    BSONMacros.handler[LastProcessedMarker]

  override protected def uniqueDocumentId: String = "last_processed"

  override protected def defaultEntity: LastProcessedMarker = LastProcessedMarker(
    timestamp = Instant.unsafeNow(),
    photoSet = PhotoSet(
      url   = new java.net.URL("http://example.com/"),
      title = PhotoSetTitle("example"),
      date  = LocalDate.unsafeToday(),
    ),
  )
}
