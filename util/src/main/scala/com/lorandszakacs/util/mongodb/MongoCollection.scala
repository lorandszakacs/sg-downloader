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
    implicit
    enH: BSONDocumentHandler[Entity],
    idH: BSONHandler[BSONTargetType, IdType],
    ec:  ExecutionContext,
    id:  Identifier[Entity, IdType]
  ): MongoCollection[Entity, IdType, BSONTargetType] = {
    new MongoCollection[Entity, IdType, BSONTargetType] {
      override protected implicit val executionContext: ExecutionContext                    = ec
      override protected implicit val entityHandler:    BSONDocumentHandler[Entity]         = enH
      override protected implicit val idHandler:        BSONHandler[BSONTargetType, IdType] = idH
      override protected implicit val identifier:       Identifier[Entity, IdType]          = id

      override val collectionName: String = collName

      override protected val db: Database = database
    }
  }

  private def interpretWriteResult(wr: WriteResult): IO[Unit] = {
    when(!wr.ok) failWith MongoDBException(
      code = wr.code.map(_.toString),
      msg  = wr.writeErrors.headOption.map(_.toString)
    )
  }

  private def interpretWriteResult(wr: MultiBulkWriteResult)(implicit ec: ExecutionContext): IO[Unit] = {
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

  protected implicit def entityHandler: BSONDocumentHandler[Entity]

  protected implicit def idHandler: BSONHandler[BSONTargetType, IdType]

  protected implicit def identifier: Identifier[Entity, IdType]

  protected def db: Database

  def collectionName: String

  lazy val collectionIO: IO[BSONCollection] = db(collectionName)

  lazy val collection: BSONCollection = collectionIO.unsafeRunSync()

  def idQuery(id: IdType): BSONDocument = BSONDocument(_id -> id)

  def idQueryByEntity(id: Entity): BSONDocument = BSONDocument(_id -> identifier.id(id))

  def findOne(query: BSONDocument): IO[Option[Entity]] = {
    collection.find(query).one[Entity].suspendInIO
  }

  def findMany(query: BSONDocument, maxDocs: Int = Int.MaxValue): IO[List[Entity]] = {
    val cursor: Cursor[Entity] = collection.find(query).cursor[Entity]()
    cursor.collect[List](maxDocs = maxDocs, err = Cursor.FailOnError[List[Entity]]()).suspendInIO
  }

  def findAll: IO[List[Entity]] = {
    this.findMany(BSONDocument.empty)
  }

  def find(id: IdType): IO[Option[Entity]] = {
    this.findOne(idQuery(id))
  }

  def findManyById(ids: Seq[IdType]): IO[List[Entity]] = {
    if (ids.isEmpty) {
      IO.pure(Nil)
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

  def create(toCreate: Entity): IO[Unit] = {
    for {
      _ <- collection.insert(toCreate).suspendInIO.discardValue.recoverWith {
            case e: LastError =>
              MongoCollection.interpretWriteResult(e)

            case NonFatal(e) =>
              IO.raiseError(e)
          }
    } yield ()
  }

  def create(toCreate: List[Entity]): IO[Unit] = {
    for {
      wr <- collection.insert[Entity](ordered = false).many(toCreate).suspendInIO
      _  <- MongoCollection.interpretWriteResult(wr)
    } yield ()
  }

  def createOrUpdate(query: BSONDocument, toCreate: Entity): IO[Unit] = {
    for {
      _ <- collection.update(query, toCreate, upsert = true).suspendInIO.discardValue.recoverWith {
            case e: LastError =>
              MongoCollection.interpretWriteResult(e)

            case NonFatal(e) =>
              IO.raiseError(e)
          }
    } yield ()
  }

  def createOrUpdate(toCreate: Entity): IO[Unit] = {
    for {
      _ <- collection.update(idQueryByEntity(toCreate), toCreate, upsert = true).suspendInIO.discardValue.recoverWith {
            case e: LastError =>
              MongoCollection.interpretWriteResult(e)

            case NonFatal(e) =>
              IO.raiseError(e)
          }
    } yield ()
  }

  def createOrUpdate(toCreateOrUpdate: List[Entity]): IO[Unit] = {
    for {
      _ <- toCreateOrUpdate.traverse[IO, Unit] { entity: Entity =>
            this.createOrUpdate(entity)
          }
    } yield ()
  }

  def remove(q: BSONDocument, firstMatchOnly: Boolean = false): IO[Unit] = {
    for {
      _ <- collection.remove(q, firstMatchOnly = firstMatchOnly).suspendInIO.discardValue.recoverWith {
            case e: LastError =>
              MongoCollection.interpretWriteResult(e)

            case NonFatal(e) =>
              IO.raiseError(e)
          }
    } yield ()
  }

  def remove(id: IdType): IO[Unit] = {
    this.remove(idQuery(id), firstMatchOnly = true)
  }

}
