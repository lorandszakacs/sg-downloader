package com.lorandszakacs.sg.model.impl

import com.lorandszakacs.sg.model._
import org.joda.time.LocalDate
import com.lorandszakacs.util.mongodb._

import com.lorandszakacs.util.future._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
private[model] class HopefulsDao(val db: Database)(implicit val ec: ExecutionContext) extends MongoDAO {

  override protected val collectionName: String = "hopefuls"

  private val repo = MongoCollection.apply[Hopeful, ModelName, BSONString](collectionName, db)

  def createOrUpdate(hopeful: Hopeful): Future[Unit] = {
    repo.createOrUpdate(repo.idQuery(hopeful.name), hopeful)
  }

  def delete(name: ModelName): Future[Unit] = {
    repo.remove(repo.idQuery(name))
  }

  def find(name: ModelName): Future[Option[Hopeful]] = {
    repo.findOne(repo.idQuery(name))
  }

  def find(names: Seq[ModelName]): Future[List[Hopeful]] = {
    repo.findManyById(names)
  }

  def findAll: Future[List[Hopeful]] = {
    repo.findAll
  }

  def findWithZeroSets: Future[List[Hopeful]] = {
    val q: BSONDocument = BSONDocument(
      "photoSets" -> BSONDocument("$size" -> 0)
    )
    repo.findMany(q)
  }

  def findBetweenDays(start: LocalDate, end: LocalDate): Future[List[Hopeful]] = {
    val q = BSONDocument(
      "photoSets.date" -> BSONDocument("$gte" -> start),
      "photoSets.date" -> BSONDocument("$lte" -> end)
    )
    repo.findMany(q)
  }

}