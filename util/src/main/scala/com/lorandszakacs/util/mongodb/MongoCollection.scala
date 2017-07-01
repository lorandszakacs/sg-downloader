package com.lorandszakacs.util.mongodb

import Imports._
import com.lorandszakacs.util.future._
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONValue

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 26 Jun 2017
  *
  */

object MongoCollection {
  def apply[Entity, IdType, BSONTargetType <: BSONValue](collName: String, database: Database)
    (implicit entityHandlerImpl: BSONDocumentHandler[Entity],
      idHandlerImpl: BSONHandler[BSONTargetType, IdType],
      ec: ExecutionContext): MongoCollection[Entity, IdType, BSONTargetType] = {
    new MongoCollection[Entity, IdType, BSONTargetType] {
      override protected implicit val executionContext: ExecutionContext = ec
      override protected implicit val objectHandler: BSONDocumentHandler[Entity] = entityHandlerImpl
      override protected implicit val idHandler: BSONHandler[BSONTargetType, IdType] = idHandlerImpl

      override val collectionName: String = collName

      override protected val db: Database = database
    }
  }

  private def interpretWriteResult(wr: WriteResult): Future[Unit] = {
    when(!wr.ok) failWith MongoDBException(code = wr.code.map(_.toString), msg = wr.writeErrors.headOption.map(_.toString))
  }
}

trait MongoCollection[Entity, IdType, BSONTargetType <: BSONValue] {
  protected implicit def executionContext: ExecutionContext

  protected implicit def objectHandler: BSONDocumentHandler[Entity]

  protected implicit def idHandler: BSONHandler[BSONTargetType, IdType]

  protected def db: Database

  def collectionName: String

  lazy val collection: BSONCollection = db(collectionName)

  def idQuery(id: IdType): BSONDocument = BSONDocument(_id -> id)

  def findOne(query: BSONDocument): Future[Option[Entity]] = {
    collection.find(query).one[Entity]
  }

  def findMany(query: BSONDocument, maxDocs: Int = Int.MaxValue): Future[List[Entity]] = {
    val cursor: Cursor[Entity] = collection.find(query).cursor[Entity]()
    cursor.collect[List](maxDocs = Int.MaxValue, err = Cursor.FailOnError[List[Entity]]())
  }

  def findAll: Future[List[Entity]] = {
    this.findMany(BSONDocument.empty)
  }

  def findById(id: IdType): Future[Option[Entity]] = {
    this.findOne(idQuery(id))
  }

  def findManyById(ids: Seq[IdType]): Future[List[Entity]] = {
    if (ids.isEmpty) {
      Future.successful(Nil)
    } else {
      val q = BSONDocument(
        _id -> BSONDocument(
          `$in` -> ids
        )
      )
      this.findMany(q)
    }
  }

  def create(toCreate: Entity): Future[Unit] = {
    for {
      wr <- collection.insert(toCreate)
      _ <- MongoCollection.interpretWriteResult(wr)
    } yield ()
  }

  def createOrUpdate(query: BSONDocument, toCreate: Entity): Future[Unit] = {
    for {
      wr <- collection.update(query, toCreate, upsert = true)
      _ <- MongoCollection.interpretWriteResult(wr)
    } yield ()
  }

  def remove(q: BSONDocument, firstMatchOnly: Boolean = false): Future[Unit] = {
    for {
      wr <- collection.remove(q, firstMatchOnly = firstMatchOnly)
      _ <- MongoCollection.interpretWriteResult(wr)
    } yield ()

  }

}