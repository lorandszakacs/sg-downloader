package com.lorandszakacs.sg.model.impl

import com.lorandszakacs.sg.model._
import com.lorandszakacs.util.future._
import com.lorandszakacs.util.mongodb._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 14 Jul 2017
  *
  */
class RepoLastProcessedMarker(override protected val db: Database)(
  implicit override val executionContext: ExecutionContext
) extends IndexSingleDocRepo[LastProcessedMarker] with ModelBSON {

  override protected def objectHandler: BSONDocumentHandler[LastProcessedMarker] = BSONMacros.handler[LastProcessedMarker]

  override protected def uniqueDocumentId: String = "last-processed"

  override protected def defaultEntity: LastProcessedMarker =
    throw new AssertionError("no such thing as a default LastProcessedMarker")
}
