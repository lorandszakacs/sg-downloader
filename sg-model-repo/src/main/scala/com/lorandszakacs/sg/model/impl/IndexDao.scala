package com.lorandszakacs.sg.model.impl

import com.lorandszakacs.sg.model._
import reactivemongo.api.{DB, DefaultDB}
import reactivemongo.bson._

import scala.concurrent.{Future, ExecutionContext}

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
final private[model] class IndexDao(val db: DB)(implicit val ec: ExecutionContext) extends MongoDAO {
  override protected val collectionName: String = "model-index"

  private val SGIndexId = "suicide-girls-index"
  private val HopefulIndexId = "hopeful-index"
  private val Names = "names"
  private val Number = "number"

  def createOrUpdateSuicideGirlsIndex(names: List[ModelName]): Future[Unit] = {
    val d = BSONDocument(
      _id -> SGIndexId,
      Names -> names.sorted,
      Number -> names.length
    )
    collection.update(selector = BSONDocument(_id -> SGIndexId), update = d, upsert = true) map { _ => () }
  }


  def createOrUpdateHopefulIndex(names: List[ModelName]): Future[Unit] = {
    val d = BSONDocument(
      _id -> HopefulIndexId,
      Names -> names.sorted,
      Number -> names.length
    )

    collection.update(selector = BSONDocument(_id -> HopefulIndexId), update = d, upsert = true) map { _ => () }
  }

  def createOrUpdateLastProcessedStatus(status: LastProcessedIndex): Future[Unit] = {
    ???
  }
}
