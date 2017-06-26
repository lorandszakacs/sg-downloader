package com.lorandszakacs.sg.model.impl

import com.lorandszakacs.sg.model._
import org.joda.time.LocalDate
import reactivemongo.api.{Cursor, DB}
import reactivemongo.bson.BSONDocument

import scala.concurrent._

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
private[model] class HopefulsDao(val db: DB)(implicit val ec: ExecutionContext) extends MongoDAO {
  override protected val collectionName: String = "hopefuls"

  def createOrUpdate(hopeful: Hopeful): Future[Unit] = {
    val q = BSONDocument(_id -> hopeful.name)
    collection.update(q, hopeful, upsert = true) map { _ => () }
  }

  def delete(name: ModelName): Future[Unit] = {
    val q = BSONDocument(_id -> name)
    collection.remove(q) map { _ => () }
  }

  def find(name: ModelName): Future[Option[Hopeful]] = {
    collection.find(BSONDocument(_id -> name)).one[Hopeful]
  }

  def find(names: Seq[ModelName]): Future[List[Hopeful]] = {
    if (names.isEmpty) {
      Future.successful(Nil)
    } else {
      val q = BSONDocument(
        _id -> BSONDocument(
          "$in" -> names
        )
      )
      val cursor: Cursor[Hopeful] = collection.find(q).cursor[Hopeful]()
      cursor.collect[List](maxDocs = Int.MaxValue, err = Cursor.FailOnError[List[Hopeful]]())
    }
  }

  def findAll: Future[List[Hopeful]] = {
    collection.find(BSONDocument()).cursor[Hopeful]().collect[List]()
  }

  def findWithZeroSets: Future[List[Hopeful]] = {
    val q: BSONDocument = BSONDocument(
      "photoSets" -> BSONDocument("$size" -> 0)
    )
    collection.find(q).cursor[Hopeful]().collect[List]()
  }

  def findBetweenDays(start: LocalDate, end: LocalDate): Future[List[Hopeful]] = {
    val q = BSONDocument(
      "photoSets.date" -> BSONDocument("$gte" -> start),
      "photoSets.date" -> BSONDocument("$lte" -> end)
    )
    collection.find(q).cursor[Hopeful]().collect[List]()
  }

}