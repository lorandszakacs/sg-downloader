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
private[impl] class RepoHFs(override protected val db: Database)(
  implicit override val executionContext:              ExecutionContext
) extends MRepo[HF](hfIdentifier) with SGRepoBSON {

  override val collectionName: String = "hfs"
  override protected implicit val entityHandler: BSONDocumentHandler[HF] =
    BSONMacros.handler[HF]

}
