package com.lorandszakacs.sg.model.impl

import com.lorandszakacs.sg.model._
import com.lorandszakacs.util.effects._
import com.lorandszakacs.util.mongodb._
import com.lorandszakacs.util.time.DateTime

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 14 Jul 2017
  *
  */
private[impl] class RepoLastProcessedMarker(override protected val db: Database)(
  implicit
  override val scheduler: DBIOScheduler
) extends IndexSingleDocRepo[LastProcessedMarker] with SGRepoBSON {

  override protected def objectHandler: BSONDocumentHandler[LastProcessedMarker] =
    BSONMacros.handler[LastProcessedMarker]

  override protected def uniqueDocumentId: String = "last_processed"

  override protected def defaultEntity: LastProcessedMarker = LastProcessedMarker(
    timestamp = DateTime.now(),
    photoSet = PhotoSet(
      url   = new java.net.URL("http://example.com/"),
      title = PhotoSetTitle("example"),
      date  = DateTime.now().toLocalDate
    )
  )
}
