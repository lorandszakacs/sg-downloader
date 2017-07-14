package com.lorandszakacs.sg.model.impl

import com.lorandszakacs.sg.model._
import org.joda.time.LocalDate
import com.lorandszakacs.util.mongodb._
import com.lorandszakacs.util.future._
import com.lorandszakacs.util.mongodb.MongoCollection

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
private[model] class SuicideGirlsDao(val db: Database)(implicit val ec: ExecutionContext) extends MongoDAO {

  override protected val collectionName: String = "suicide_girls"

  private val repo = MongoCollection.apply[SuicideGirl, ModelName, BSONString](collectionName, db)

  def createOrUpdate(sg: SuicideGirl): Future[Unit] = {
    //    val q = BSONDocument(_id -> sg.name)
    //    collection.update(q, sg, upsert = true) map { _ => () }
    repo.createOrUpdate(repo.idQuery(sg.name), sg)
  }

  def find(name: ModelName): Future[Option[SuicideGirl]] = {
    //    collection.find(BSONDocument(_id -> name)).one[SuicideGirl]
    repo.findOne(repo.idQuery(name))
  }

  def find(names: Seq[ModelName]): Future[List[SuicideGirl]] = {
    //    if (names.isEmpty) {
    //      Future.successful(Nil)
    //    } else {
    //      val q = BSONDocument(
    //        _id -> BSONDocument(
    //          "$in" -> names
    //        )
    //      )
    //      collection.find(q).cursor[SuicideGirl]().collect[List]()
    //    }
    repo.findManyById(names)
  }

  def findAll: Future[List[SuicideGirl]] = {
    repo.findAll
    //    collection.find(BSONDocument()).cursor[SuicideGirl]().collect[List]()
  }

  def delete(name: ModelName): Future[Unit] = {
    repo.remove(repo.idQuery(name))
    //    collection.remove(BSONDocument(_id -> name)).map { _ => () }
  }

  def findWithZeroSets: Future[List[SuicideGirl]] = {
    val q: BSONDocument = BSONDocument(
      "photoSets" -> BSONDocument($size -> 0)
    )

    //    collection.find(q).cursor[SuicideGirl]().collect[List]()
    repo.findMany(q)

  }

  def findBetweenDays(start: LocalDate, end: LocalDate): Future[List[SuicideGirl]] = {
    val q = BSONDocument(
      "photoSets.date" -> BSONDocument($gte -> start),
      "photoSets.date" -> BSONDocument($lte -> end)
    )
    //    collection.find(q).cursor[SuicideGirl]().collect[List]()
    repo.findMany(q)

  }
}
