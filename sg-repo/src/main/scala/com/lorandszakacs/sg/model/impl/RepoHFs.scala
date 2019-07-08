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
private[impl] class RepoHFs(override protected val db: Database)(
  implicit
  override val dbIOScheduler: DBIOScheduler,
  override val futureLift: FutureLift[Task],
) extends MRepo[HF](hfIdentifier) with SGRepoBSON {

  override val collectionName: String = "hfs"
  implicit override protected val entityHandler: BSONDocumentHandler[HF] =
    BSONMacros.handler[HF]

}
