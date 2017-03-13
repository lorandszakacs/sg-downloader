package com.lorandszakacs.sg.model.impl

import com.lorandszakacs.sg.model._
import org.joda.time.LocalDate
import reactivemongo.api.DB
import reactivemongo.bson.{BSONDocument, _}

import scala.concurrent._

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
private[model] class SuicideGirlsDao(val db: DB)(implicit val ec: ExecutionContext) extends MongoDAO {

  override protected val collectionName: String = "suicide_girls"

  def createOrUpdate(sg: SuicideGirl): Future[Unit] = {
    val q = BSONDocument(_id -> sg.name)
    collection.update(q, sg, upsert = true) map { _ => () }
  }

  def find(name: ModelName): Future[Option[SuicideGirl]] = {
    collection.find(BSONDocument(_id -> name)).one[SuicideGirl]
  }

  def find(names: Seq[ModelName]): Future[List[SuicideGirl]] = {
    if (names.isEmpty) {
      Future.successful(Nil)
    } else {
      val q = BSONDocument(
        _id -> BSONDocument(
          "$in" -> names
        )
      )
      collection.find(q).cursor[SuicideGirl]().collect[List]()
    }
  }

  def findAll: Future[List[SuicideGirl]] = {
    collection.find(BSONDocument()).cursor[SuicideGirl]().collect[List]()
  }

  def delete(name: ModelName): Future[Unit] = {
    collection.remove(BSONDocument(_id -> name)).map { _ => () }
  }

  def findWithZeroSets: Future[List[SuicideGirl]] = {
    val q: BSONDocument = BSONDocument(
      "photoSets" -> BSONDocument("$size" -> 0)
    )
    collection.find(q).cursor[SuicideGirl]().collect[List]()
  }

  def findBetweenDays(start: LocalDate, end: LocalDate): Future[List[SuicideGirl]] = {
    val q = BSONDocument(
      "photoSets.date" -> BSONDocument("$gte" -> start),
      "photoSets.date" -> BSONDocument("$lte" -> end)
    )
    collection.find(q).cursor[SuicideGirl]().collect[List]()
  }
}
