package com.lorandszakacs.util.mongodb

import com.lorandszakacs.util.future._
import com.lorandszakacs.util.math.Identifier
import reactivemongo.api.commands.{LastError, MultiBulkWriteResult, WriteResult}

import scala.util.control.NonFatal

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 26 Jun 2017
  *
  */

object MongoCollection {

  def apply[Entity, IdType, BSONTargetType <: BSONValue](collName: String, database: Database)(
    implicit entityHandlerImpl:                                    BSONDocumentHandler[Entity],
    idHandlerImpl:                                                 BSONHandler[BSONTargetType, IdType],
    ec:                                                            ExecutionContext,
    identifierImpl:                                                Identifier[Entity, IdType]
  ): MongoCollection[Entity, IdType, BSONTargetType] = {
    new MongoCollection[Entity, IdType, BSONTargetType] {
      override protected implicit val executionContext: ExecutionContext                    = ec
      override protected implicit val objectHandler:    BSONDocumentHandler[Entity]         = entityHandlerImpl
      override protected implicit val idHandler:        BSONHandler[BSONTargetType, IdType] = idHandlerImpl
      override protected implicit val identifier:       Identifier[Entity, IdType]          = identifierImpl

      override val collectionName: String = collName

      override protected val db: Database = database
    }
  }

  private def interpretWriteResult(wr: WriteResult): Future[Unit] = {
    when(!wr.ok) failWith MongoDBException(
      code = wr.code.map(_.toString),
      msg  = wr.writeErrors.headOption.map(_.toString)
    )
  }

  private def interpretWriteResult(wr: MultiBulkWriteResult)(implicit ec: ExecutionContext): Future[Unit] = {
    for {
      _ <- when(!wr.ok) failWith MongoDBException(
            code = wr.code.map(_.toString),
            msg  = wr.writeErrors.headOption.map(_.toString)
          )
      _ <- when(wr.writeErrors.nonEmpty) failWith MongoDBException(
            code = wr.code.map(_.toString),
            msg  = wr.writeErrors.headOption.map(_.toString)
          )
    } yield ()

  }
}

trait MongoCollection[Entity, IdType, BSONTargetType <: BSONValue] {
  protected implicit def executionContext: ExecutionContext

  protected implicit def objectHandler: BSONDocumentHandler[Entity]

  protected implicit def idHandler: BSONHandler[BSONTargetType, IdType]

  protected implicit def identifier: Identifier[Entity, IdType]

  protected def db: Database

  def collectionName: String

  lazy val collection: BSONCollection = db(collectionName)

  def idQuery(id: IdType): BSONDocument = BSONDocument(_id -> id)

  def idQueryByEntity(id: Entity): BSONDocument = BSONDocument(_id -> identifier.id(id))

  def findOne(query: BSONDocument): Future[Option[Entity]] = {
    collection.find(query).one[Entity]
  }

  def findMany(query: BSONDocument, maxDocs: Int = Int.MaxValue): Future[List[Entity]] = {
    val cursor: Cursor[Entity] = collection.find(query).cursor[Entity]()
    cursor.collect[List](maxDocs = maxDocs, err = Cursor.FailOnError[List[Entity]]())
  }

  def findAll: Future[List[Entity]] = {
    this.findMany(BSONDocument.empty)
  }

  def find(id: IdType): Future[Option[Entity]] = {
    this.findOne(idQuery(id))
  }

  def findManyById(ids: Seq[IdType]): Future[List[Entity]] = {
    if (ids.isEmpty) {
      Future.successful(Nil)
    }
    else {
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
      _ <- collection.insert(toCreate) recoverWith {
            case e: LastError =>
              MongoCollection.interpretWriteResult(e)

            case NonFatal(e) =>
              Future.failed(e)
          }
    } yield ()
  }

  def create(toCreate: List[Entity]): Future[Unit] = {
    val bulkDocs =
      toCreate.map(implicitly[collection.ImplicitlyDocumentProducer](_))
    for {
      wr <- collection.bulkInsert(ordered = false)(bulkDocs: _*)
      _  <- MongoCollection.interpretWriteResult(wr)
    } yield ()
  }

  def createOrUpdate(query: BSONDocument, toCreate: Entity): Future[Unit] = {
    for {
      _ <- collection.update(query, toCreate, upsert = true) recoverWith {
            case e: LastError =>
              MongoCollection.interpretWriteResult(e)

            case NonFatal(e) =>
              Future.failed(e)
          }
    } yield ()
  }

  def createOrUpdate(toCreate: Entity): Future[Unit] = {
    for {
      _ <- collection.update(idQueryByEntity(toCreate), toCreate, upsert = true) recoverWith {
            case e: LastError =>
              MongoCollection.interpretWriteResult(e)

            case NonFatal(e) =>
              Future.failed(e)
          }
    } yield ()
  }

  def createOrUpdate(toCreateOrUpdate: List[Entity]): Future[Unit] = {
    for {
      _ <- Future.traverse(toCreateOrUpdate) { entity =>
            this.createOrUpdate(entity)
          }
    } yield ()
  }

  def remove(q: BSONDocument, firstMatchOnly: Boolean = false): Future[Unit] = {
    for {
      _ <- collection.remove(q, firstMatchOnly = firstMatchOnly) recoverWith {
            case e: LastError =>
              MongoCollection.interpretWriteResult(e)

            case NonFatal(e) =>
              Future.failed(e)
          }
    } yield ()
  }

  def remove(id: IdType): Future[Unit] = {
    this.remove(idQuery(id), firstMatchOnly = true)
  }

}
