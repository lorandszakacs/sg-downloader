package com.lorandszakacs.sg.model.impl

import com.lorandszakacs.sg.model._
import com.lorandszakacs.util.effects._
import com.lorandszakacs.util.mongodb._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 14 Jul 2017
  *
  */
private[impl] class RepoSGIndex(override protected val db: Database)(
  implicit
  override val dbIOScheduler: DBIOScheduler
) extends IndexSingleDocRepo[SGIndex] with SGRepoBSON {

  override protected def objectHandler: BSONDocumentHandler[SGIndex] = BSONMacros.handler[SGIndex]

  override protected def uniqueDocumentId: String = "sg_index"

  override protected def defaultEntity: SGIndex = SGIndex(
    names           = Nil,
    needsReindexing = Nil,
    number          = 0
  )

  private def sanitize(i: SGIndex): SGIndex = {
    val temp = i.names.distinct.sorted
    i.copy(
      names           = temp,
      needsReindexing = i.needsReindexing.distinct.sorted,
      number          = temp.size
    )
  }

  private def sanitize(names: List[Name]): SGIndex = {
    val temp = names.distinct.sorted
    SGIndex(
      names           = temp,
      needsReindexing = temp,
      number          = temp.length
    )
  }

  override def create(sg: SGIndex): Task[Unit] = {
    super.create(sanitize(sg))
  }

  override def createOrUpdate(sg: SGIndex): Task[Unit] = {
    super.createOrUpdate(sanitize(sg))
  }

  def rewriteIndex(names: List[Name]): Task[Unit] = {
    this.createOrUpdate(sanitize(names))
  }

}
