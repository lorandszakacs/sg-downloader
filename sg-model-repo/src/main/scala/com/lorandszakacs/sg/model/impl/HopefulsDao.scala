package com.lorandszakacs.sg.model.impl

import com.lorandszakacs.sg.model._
import reactivemongo.api.DB
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

  def find(name: ModelName): Future[Option[Hopeful]] = {
    collection.find(BSONDocument(_id -> name)).one[Hopeful]
  }
}