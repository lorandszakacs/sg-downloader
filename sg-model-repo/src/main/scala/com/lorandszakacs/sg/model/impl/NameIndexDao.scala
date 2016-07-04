package com.lorandszakacs.sg.model.impl

import com.lorandszakacs.sg.model.SuicideGirlIndex
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{DB, DefaultDB}
import reactivemongo.bson._

import scala.concurrent.{Future, ExecutionContext}

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
final private[model] class NameIndexDao(val db: DB)(implicit val ec: ExecutionContext) extends MongoDAO {
  override protected val collectionName: String = "model-index"

  private val SGIndexId = "suicide-girls-index"
  private val HopefulIndexId = "hopeful-index"
  private val Names = "names"
  private val Number = "number"

  private val suicideGirlsIndexBSON: BSONDocumentReader[SuicideGirlIndex] with BSONDocumentWriter[SuicideGirlIndex] with BSONHandler[BSONDocument, SuicideGirlIndex] =
    Macros.handler[SuicideGirlIndex]

  def createOrUpdateSuicideGirlsIndex(names: List[String]): Future[Unit] = {
    val d = BSONDocument(
      _id -> SGIndexId,
      "names" -> names.sorted,
      "number" -> names.length
    )

    collection.update(selector = BSONDocument(_id -> SGIndexId), update = d, upsert = true) map { _ => () }
  }


  def createOrUpdateHopefulIndex(names: List[String]): Future[Unit] = {
    val d = BSONDocument(
      _id -> HopefulIndexId,
      "names" -> names.sorted,
      "number" -> names.length
    )

    collection.update(selector = BSONDocument(_id -> HopefulIndexId), update = d, upsert = true) map { _ => () }
  }
}
