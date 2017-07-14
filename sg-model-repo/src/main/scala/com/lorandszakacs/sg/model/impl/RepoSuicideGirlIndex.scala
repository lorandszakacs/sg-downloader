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
class RepoSuicideGirlIndex(override protected val db: Database)(
  implicit override val executionContext: ExecutionContext
) extends IndexSingleDocRepo[SuicideGirlIndex] with ModelBSON {

  override protected def objectHandler: BSONDocumentHandler[SuicideGirlIndex] = BSONMacros.handler[SuicideGirlIndex]

  override protected def uniqueDocumentId: String = "suicide-girls-index"

  override protected def defaultEntity: SuicideGirlIndex = SuicideGirlIndex(
    names = Nil,
    needsReindexing = Nil,
    number = 0
  )

  private def sanitize(i: SuicideGirlIndex): SuicideGirlIndex = {
    val temp = i.names.distinct.sorted
    i.copy(
      names = temp,
      needsReindexing = i.needsReindexing.distinct.sorted,
      number = temp.size
    )
  }

  private def sanitize(names: List[ModelName]): SuicideGirlIndex = {
    val temp = names.distinct.sorted
    SuicideGirlIndex(
      names = temp,
      needsReindexing = temp,
      number = temp.length
    )
  }

  override def create(sg: SuicideGirlIndex): Future[Unit] = {
    super.create(sanitize(sg))
  }

  override def createOrUpdate(sg: SuicideGirlIndex): Future[Unit] = {
    super.createOrUpdate(sanitize(sg))
  }

  def rewriteIndex(names: List[ModelName]): Future[Unit] = {
    this.createOrUpdate(sanitize(names))
  }

}
