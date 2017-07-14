package com.lorandszakacs.util.mongodb

import com.lorandszakacs.util.future._

/**
  *
  * A convenience trait for collections that have one single document
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 14 Jul 2017
  *
  */
trait SingleDocumentMongoCollection[Entity, IdType, BSONTargetType <: BSONValue] {

  protected implicit def executionContext: ExecutionContext

  protected implicit def objectHandler: BSONDocumentHandler[Entity]

  protected implicit def idHandler: BSONHandler[BSONTargetType, IdType]

  protected def uniqueDocumentId: IdType

  protected def db: Database

  def collectionName: String

  private lazy val repo: MongoCollection[Entity, IdType, BSONTargetType] =
    MongoCollection(collectionName, db)

  def idQuery: BSONDocument = repo.idQuery(uniqueDocumentId)

  def create(e: Entity): Future[Unit] =
    repo.create(e)

  def createOrUpdate(e: Entity): Future[Unit] =
    repo.create(e)

  def rewrite(e: Entity): Future[Unit] =
    repo.createOrUpdate(idQuery, e)

  def remove(): Future[Unit] =
    repo.remove(idQuery)

  def find: Future[Option[Entity]] =
    repo.findOne(idQuery)

}
