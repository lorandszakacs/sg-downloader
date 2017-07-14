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
private[impl] class RepoCleanedUpModelsIndex(override protected val db: Database)(
  implicit override val executionContext: ExecutionContext
) extends IndexSingleDocRepo[CleanedUpModelsIndex] with ModelBSON {

  override protected def objectHandler: BSONDocumentHandler[CleanedUpModelsIndex] = BSONMacros.handler[CleanedUpModelsIndex]

  override protected def uniqueDocumentId: String = "cleaned-up-index"

  override protected def defaultEntity: CleanedUpModelsIndex =
    CleanedUpModelsIndex(Nil, Nil)
}
