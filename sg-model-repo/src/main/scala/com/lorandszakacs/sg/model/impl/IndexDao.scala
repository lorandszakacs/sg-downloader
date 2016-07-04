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
  private val LastProcessedId = "last-processed"

  private val Names = "names"
  private val Number = "number"
  private val NeedsReindexing = "needsReindexing"

  def suicideGirlsIndex: Future[SuicideGirlIndex] = {
    val q = BSONDocument(_id -> SGIndexId)
    collection.find(q).one[SuicideGirlIndex] map {
      _.getOrElse(SuicideGirlIndex(Nil, Nil, 0))
    }
  }

  def reindexSGs(names: List[ModelName]): Future[Unit] = {
    val sorted = names.distinct.sorted
    val d = BSONDocument(
      _id -> SGIndexId,
      Names -> sorted,
      NeedsReindexing -> sorted,
      Number -> names.length
    )
    val q = BSONDocument(_id -> SGIndexId)

    collection.update(selector = q, update = d, upsert = true) map { _ => () }
  }

  def updateSGIndex(i: SuicideGirlIndex): Future[SuicideGirlIndex] = {
    val sanitizedNames = i.names.distinct.sorted
    val sanitizedIndex = i.copy(
      names = sanitizedNames,
      needsReindexing = i.names.distinct.sorted,
      number = sanitizedNames.length
    )
    val q = BSONDocument(_id -> SGIndexId)

    collection.update(selector = q, update = sanitizedIndex, upsert = true) map { _ => sanitizedIndex }
  }


  def hopefulIndex: Future[HopefulIndex] = {
    val q = BSONDocument(_id -> HopefulIndexId)
    collection.find(q).one[HopefulIndex] map {
      _.getOrElse(HopefulIndex(Nil, Nil, 0))
    }
  }


  def reindexHopefuls(names: List[ModelName]): Future[Unit] = {
    val sanitized = names.distinct.sorted
    val d = BSONDocument(
      _id -> HopefulIndexId,
      Names -> sanitized,
      NeedsReindexing -> sanitized,
      Number -> sanitized.length
    )

    collection.update(selector = BSONDocument(_id -> HopefulIndexId), update = d, upsert = true) map { _ => () }
  }

  def updateHopefulIndex(i: HopefulIndex): Future[HopefulIndex] = {
    val sanitizedNames = i.names.distinct.sorted
    val sanitizedIndex = i.copy(
      names = sanitizedNames,
      needsReindexing = i.names.distinct.sorted,
      number = sanitizedNames.length
    )
    val q = BSONDocument(_id -> HopefulIndexId)

    collection.update(selector = q, update = sanitizedIndex, upsert = true) map { _ => sanitizedIndex }
  }

  def createOrUpdateLastProcessedStatus(status: LastProcessedMarker): Future[Unit] = {
    val d: BSONDocument = lastProcessedStatusBSON.write(status) ++ (_id -> LastProcessedId)
    val s: BSONDocument = BSONDocument(_id -> LastProcessedId)

    collection.update(selector = s, update = d, upsert = true) map { _ => () }
  }

  def lastProcessedStatus: Future[Option[LastProcessedMarker]] = {
    val q: BSONDocument = BSONDocument(_id -> LastProcessedId)
    collection.find(q).one[LastProcessedMarker]
  }
}
