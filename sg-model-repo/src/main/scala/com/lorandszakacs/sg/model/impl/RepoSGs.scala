package com.lorandszakacs.sg.model.impl

import com.lorandszakacs.sg.model._
import com.lorandszakacs.util.mongodb._
import com.lorandszakacs.util.future._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 14 Jul 2017
  *
  */
private[impl] class RepoSGs(override protected val db: Database)(
  implicit override val executionContext:              ExecutionContext
) extends MRepo[SG](sgIdentifier) with ModelBSON {

  override val collectionName: String = "sgs"
  override protected implicit val objectHandler: BSONDocumentHandler[SG] =
    BSONMacros.handler[SG]
}
