package com.lorandszakacs.util.mongodb

import com.lorandszakacs.util.effects._
import com.lorandszakacs.util.math.Identifier

/**
  *
  * A convenience trait for collections that have one single document
  * with a unique, and stable ID.
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 14 Jul 2017
  *
  */
trait SingleDocumentMongoCollection[Entity, IdType, BSONTargetType <: BSONValue] {

  protected implicit def dbIOScheduler: DBIOScheduler

  protected def objectHandler: BSONDocumentHandler[Entity]

  protected implicit def idHandler: BSONHandler[BSONTargetType, IdType]

  protected implicit def identifier: Identifier[Entity, IdType] = new Identifier[Entity, IdType] {
    override def id(t: Entity): IdType = uniqueDocumentId
  }

  protected implicit def objectHandlerWithUniqueId: BSONDocumentHandler[Entity] = {
    new BSONDocumentReader[Entity] with BSONDocumentWriter[Entity] with BSONHandler[BSONDocument, Entity] {
      override def write(t: Entity): BSONDocument =
        objectHandler.write(t).++(_id -> idHandler.write(uniqueDocumentId))

      override def read(bson: BSONDocument): Entity =
        objectHandler.read(bson)
    }
  }

  protected def uniqueDocumentId: IdType

  protected def defaultEntity: Entity

  protected def db: Database

  def collectionName: String

  private lazy val repo: MongoCollection[Entity, IdType, BSONTargetType] =
    MongoCollection(collectionName, db)

  def idQuery: BSONDocument = repo.idQuery(uniqueDocumentId)

  def create(e: Entity): Task[Unit] =
    repo.create(e)

  def createOrUpdate(e: Entity): Task[Unit] =
    repo.createOrUpdate(e)

  def remove(): Task[Unit] =
    repo.remove(idQuery)

  def find: Task[Option[Entity]] =
    repo.findOne(idQuery)

  def get: Task[Entity] = this.find.map(_.getOrElse(defaultEntity))

}
