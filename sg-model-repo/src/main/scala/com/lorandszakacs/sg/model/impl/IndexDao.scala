package com.lorandszakacs.sg.model.impl

import com.lorandszakacs.sg.model._
import com.lorandszakacs.util.mongodb.Imports._
import com.lorandszakacs.util.future._
import com.lorandszakacs.util.mongodb.MongoCollection

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
final private[model] class IndexDao(val db: Database)(implicit val ec: ExecutionContext) extends MongoDAO {
  override protected val collectionName: String = "models_index"

  /**
    * final case class CleanedUpModelsIndex(
    * suicideGirls: List[ModelName],
    * hopefuls: List[ModelName],
    * lastCleaning: DateTime
    */

  private val Names = "names"
  private val Number = "number"
  private val NeedsReindexing = "needsReindexing"

  def suicideGirlsIndex: Future[SuicideGirlIndex] = {
    val q = BSONDocument(_id -> SuicideGirlsIndexId)
    collection.find(q).one[SuicideGirlIndex] map {
      _.getOrElse(SuicideGirlIndex(Nil, Nil, 0))
    }
  }

  def rewriteSGIndex(names: List[ModelName]): Future[Unit] = {
    val sanitized = names.distinct.sorted
    val d = BSONDocument(
      _id -> SuicideGirlsIndexId,
      Names -> sanitized,
      NeedsReindexing -> sanitized,
      Number -> sanitized.length
    )
    val q = BSONDocument(_id -> SuicideGirlsIndexId)

    collection.update(selector = q, update = d, upsert = true) map { _ => () }
  }

  def rewriteSGIndex(i: SuicideGirlIndex): Future[SuicideGirlIndex] = {
    val sanitizedNames = i.names.distinct.sorted
    val sanitizedIndex = i.copy(
      names = sanitizedNames,
      needsReindexing = i.needsReindexing.distinct.sorted,
      number = sanitizedNames.length
    )
    val q = BSONDocument(_id -> SuicideGirlsIndexId)

    collection.update(selector = q, update = sanitizedIndex, upsert = true) map { _ => sanitizedIndex }
  }

  def markSGAsIndexed(name: ModelName): Future[Unit] = {
    val q = BSONDocument(_id -> SuicideGirlsIndexId)
    val u = BSONDocument(
      "$pull" -> BSONDocument(
        NeedsReindexing -> name
      )
    )
    collection.update(q, u, upsert = false) map (_ => ())
  }

  def cleanUp(suicideGirls: List[ModelName], hopefuls: List[ModelName]): Future[Unit] = {
    for {
      sgIndex <- suicideGirlsIndex
      hIndex <- hopefulIndex

      newSgIndex = {
        val newNames = sgIndex.names.filterNot(sgName => suicideGirls.contains(sgName))
        sgIndex.copy(
          names = newNames,
          needsReindexing = sgIndex.needsReindexing.filterNot(sgName => suicideGirls.contains(sgName)),
          number = newNames.size
        )
      }

      newHopefulIndexIndex = {
        val newNames = hIndex.names.filterNot(hopefulName => hopefuls.contains(hopefulName))
        hIndex.copy(
          names = newNames,
          needsReindexing = hIndex.needsReindexing.filterNot(hopefulName => hopefuls.contains(hopefulName)),
          number = newNames.size
        )
      }

      previousCleanedUp: Option[CleanedUpModelsIndex] <- collection.find(selector = BSONDocument(_id -> CleanedUpIndexId)).one[CleanedUpModelsIndex]
      newCleanedUp: CleanedUpModelsIndex = previousCleanedUp match {
        case None => CleanedUpModelsIndex(
          suicideGirls = suicideGirls,
          hopefuls = hopefuls
        )
        case Some(x) => CleanedUpModelsIndex(
          suicideGirls = (x.suicideGirls ++ suicideGirls).distinct,
          hopefuls = (x.hopefuls ++ hopefuls).distinct
        )
      }
      _ <- collection.update(selector = BSONDocument(_id -> CleanedUpIndexId), update = newCleanedUp, upsert = true) map { _ => () }
      _ <- rewriteSGIndex(newSgIndex)
      _ <- rewriteHopefulsIndex(newHopefulIndexIndex)
    } yield ()
  }


  def hopefulIndex: Future[HopefulIndex] = {
    val q = BSONDocument(_id -> HopefulIndexId)
    collection.find(q).one[HopefulIndex] map {
      _.getOrElse(HopefulIndex(Nil, Nil, 0))
    }
  }


  def rewriteHopefulsIndex(names: List[ModelName]): Future[Unit] = {
    val sanitized = names.distinct.sorted
    val d = BSONDocument(
      _id -> HopefulIndexId,
      Names -> sanitized,
      NeedsReindexing -> sanitized,
      Number -> sanitized.length
    )

    collection.update(selector = BSONDocument(_id -> HopefulIndexId), update = d, upsert = true) map { _ => () }
  }

  def rewriteHopefulsIndex(i: HopefulIndex): Future[HopefulIndex] = {
    val sanitizedNames = i.names.distinct.sorted
    val sanitizedIndex = i.copy(
      names = sanitizedNames,
      needsReindexing = i.needsReindexing.distinct.sorted,
      number = sanitizedNames.length
    )
    val q = BSONDocument(_id -> HopefulIndexId)

    collection.update(selector = q, update = sanitizedIndex, upsert = true) map { _ => sanitizedIndex }
  }

  def markHopefulAsIndexed(name: ModelName): Future[Unit] = {
    val q = BSONDocument(_id -> HopefulIndexId)
    val u = BSONDocument(
      "$pull" -> BSONDocument(
        NeedsReindexing -> name
      )
    )
    collection.update(q, u, upsert = false) map (_ => ())
  }


  def createOrUpdateLastProcessedStatus(status: LastProcessedMarker): Future[Unit] = {
    val temp: BSONDocument = status match {
      case h: LastProcessedHopeful => lastProcessedHopefulBSON.write(h)
      case sg: LastProcessedSG => lastProcessedSuicideGirlBSON.write(sg)
    }
    val d = temp ++ (_id -> LastProcessedId)

    val s: BSONDocument = BSONDocument(_id -> LastProcessedId)

    collection.update(selector = s, update = d, upsert = true) map { _ => () }
  }

  def lastProcessedStatus: Future[Option[LastProcessedMarker]] = {
    val q: BSONDocument = BSONDocument(_id -> LastProcessedId)
    collection.find(q).one[LastProcessedMarker]
  }
}
