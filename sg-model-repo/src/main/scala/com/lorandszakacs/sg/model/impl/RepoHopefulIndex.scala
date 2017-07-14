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
private[impl] class RepoHopefulIndex(override protected val db: Database)(
  implicit override val executionContext: ExecutionContext
) extends IndexSingleDocRepo[HopefulIndex] with ModelBSON {

  override protected def objectHandler: BSONDocumentHandler[HopefulIndex] = BSONMacros.handler[HopefulIndex]

  override protected def uniqueDocumentId: String = "hopefuls-index"

  override protected def defaultEntity: HopefulIndex = HopefulIndex(
    names = Nil,
    needsReindexing = Nil,
    number = 0
  )

  private def sanitize(i: HopefulIndex): HopefulIndex = {
    val temp = i.names.distinct.sorted
    i.copy(
      names = temp,
      needsReindexing = i.needsReindexing.distinct.sorted,
      number = temp.size
    )
  }

  private def sanitize(names: List[ModelName]): HopefulIndex = {
    val temp = names.distinct.sorted
    HopefulIndex(
      names = temp,
      needsReindexing = temp,
      number = temp.length
    )
  }

  override def create(sg: HopefulIndex): Future[Unit] = {
    super.create(sanitize(sg))
  }

  override def createOrUpdate(sg: HopefulIndex): Future[Unit] = {
    super.createOrUpdate(sanitize(sg))
  }

  def rewriteIndex(names: List[ModelName]): Future[Unit] = {
    this.createOrUpdate(sanitize(names))
  }

}
