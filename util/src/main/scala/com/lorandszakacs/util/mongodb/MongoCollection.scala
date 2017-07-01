package com.lorandszakacs.util.mongodb

import Imports._
import com.lorandszakacs.util.future._
import reactivemongo.api.commands.WriteResult

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 26 Jun 2017
  *
  */

object MongoCollection {
  def apply[T](collName: String, database: Database)(implicit handler: BSONDocumentHandler[T], ec: ExecutionContext) = new MongoCollection[T] {
    override protected implicit val executionContext: ExecutionContext = ec
    override protected implicit val objectHandler: BSONDocumentHandler[T] = handler

    override val collectionName: String = collName

    override protected val db: Database = database
  }

  private def interpretWriteResult(wr: WriteResult): Future[Unit] = {
    when(!wr.ok) failWith MongoDBException(code = wr.code.map(_.toString), msg = wr.writeErrors.headOption.map(_.toString))
  }
}

trait MongoCollection[T] {
  protected implicit def executionContext: ExecutionContext

  protected implicit def objectHandler: BSONDocumentHandler[T]

  protected def db: Database

  def collectionName: String

  lazy val collection: BSONCollection = db(collectionName)

  def findOne(query: BSONDocument): Future[Option[T]] = {
    collection.find(query).one[T]
  }

  def findMany(query: BSONDocument, maxDocs: Int = Int.MaxValue): Future[List[T]] = {
    val cursor: Cursor[T] = collection.find(query).cursor[T]()
    cursor.collect[List](maxDocs = Int.MaxValue, err = Cursor.FailOnError[List[T]]())
  }

  def findAll: Future[List[T]] = {
    this.findMany(BSONDocument.empty)
  }

  def findById[TypeOfID, TargetType <: BSONValue](id: TypeOfID)(implicit handler: BSONWriter[TypeOfID, TargetType]): Future[Option[T]] = {
    this.findOne(BSONDocument(_id -> id))
  }

  def create(toCreate: T): Future[Unit] = {
    for {
      wr <- collection.insert(toCreate)
      _ <- MongoCollection.interpretWriteResult(wr)
    } yield ()
  }

  def createOrUpdate(query: BSONDocument, toCreate: T): Future[Unit] = {
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