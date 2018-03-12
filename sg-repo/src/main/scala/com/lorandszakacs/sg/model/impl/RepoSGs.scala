package com.lorandszakacs.sg.model.impl

import com.lorandszakacs.sg.model._
import com.lorandszakacs.util.mongodb._
import com.lorandszakacs.util.effects._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 14 Jul 2017
  *
  */
private[impl] class RepoSGs(override protected val db: Database)(
  implicit
  override val dbIOScheduler: DBIOScheduler
) extends MRepo[SG](sgIdentifier) with SGRepoBSON {

  override val collectionName: String = "sgs"
  override protected implicit val entityHandler: BSONDocumentHandler[SG] =
    BSONMacros.handler[SG]
}