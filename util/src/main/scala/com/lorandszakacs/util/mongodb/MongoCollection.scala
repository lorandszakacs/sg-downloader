package com.lorandszakacs.util.mongodb

import com.lorandszakacs.util.effects._
import com.lorandszakacs.util.math.Identifier
import reactivemongo.api.commands.{LastError, MultiBulkWriteResult, WriteResult}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 26 Jun 2017
  *
  */
object MongoCollection {

  def apply[Entity, IdType, BSONTargetType <: BSONValue](collName: String, database: Database)(
    implicit
    enH:  BSONDocumentHandler[Entity],
    idH:  BSONHandler[BSONTargetType, IdType],
    sch:  DBIOScheduler,
    id:   Identifier[Entity, IdType],
    futL: FutureLift[IO],
  ): MongoCollection[Entity, IdType, BSONTargetType] = {
    new MongoCollection[Entity, IdType, BSONTargetType] {

      implicit override protected val dbIOScheduler: DBIOScheduler                       = sch
      implicit override protected val entityHandler: BSONDocumentHandler[Entity]         = enH
      implicit override protected val idHandler:     BSONHandler[BSONTargetType, IdType] = idH
      implicit override protected val identifier:    Identifier[Entity, IdType]          = id
      implicit override protected val futureLift:    FutureLift[IO]                      = futL

      override val collectionName: String = collName

      override protected val db: Database = database
    }
  }

  private def interpretWriteResult(wr: WriteResult): IO[Unit] = {
    wr.ok.ifFalseRaise[IO](
      MongoDBException(
        code = wr.code.map(_.toString),
        msg  = wr.writeErrors.headOption.map(_.toString),
      ),
    )
  }

  private def interpretWriteResult(wr: MultiBulkWriteResult): IO[Unit] = {
    for {
      _ <- wr.ok.ifFalseRaise[IO](
        MongoDBException(
          code = wr.code.map(_.toString),
          msg  = wr.writeErrors.headOption.map(_.toString),
        ),
      )
      _ <- wr.writeErrors.nonEmpty.ifTrueRaise[IO](
        MongoDBException(
          code = wr.code.map(_.toString),
          msg  = wr.writeErrors.headOption.map(_.toString),
        ),
      )
    } yield ()

  }
}

trait MongoCollection[Entity, IdType, BSONTargetType <: BSONValue] {
  implicit protected def dbIOScheduler: DBIOScheduler

  implicit protected def futureLift: FutureLift[IO]

  implicit protected def entityHandler: BSONDocumentHandler[Entity]

  implicit protected def idHandler: BSONHandler[BSONTargetType, IdType]

  implicit protected def identifier: Identifier[Entity, IdType]

  protected def db: Database

  def collectionName: String

  private lazy val collectionIO: IO[BSONCollection] = db.collection(collectionName)

  private lazy val collection: BSONCollection = collectionIO.unsafeSyncGet()

  def idQuery(id: IdType): BSONDocument = BSONDocument(_id -> id)

  def idQueryByEntity(id: Entity): BSONDocument = BSONDocument(_id -> identifier.id(id))

  def findOne(query: BSONDocument): IO[Option[Entity]] = {
    collection.find[BSONDocument, BSONDocument](selector = query, projection = Option.empty).one[Entity].purifyIn[IO]
  }

  def findMany(query: BSONDocument, maxDocs: Int = Int.MaxValue): IO[List[Entity]] = {
    val cursor: Cursor[Entity] =
      collection.find[BSONDocument, BSONDocument](query, projection = Option.empty).cursor[Entity]()
    cursor.collect[List](maxDocs = maxDocs, err = Cursor.FailOnError[List[Entity]]()).purifyIn[IO]
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
          `$in` -> ids,
        ),
      )
      this.findMany(q)
    }
  }

  def create(toCreate: Entity): IO[Unit] = {
    for {
      _ <- collection.insert(ordered = false).one(toCreate).purifyIn[IO].void.recoverWith {
        case e: LastError =>
          MongoCollection.interpretWriteResult(e)

        case NonFatal(e) =>
          IO.raiseError(e)
      }
    } yield ()
  }

  def create(toCreate: List[Entity]): IO[Unit] = {
    for {
      wr <- collection.insert(ordered = false).many[Entity](toCreate).purifyIn[IO]
      _  <- MongoCollection.interpretWriteResult(wr)
    } yield ()
  }

  def createOrUpdate(query: BSONDocument, toCreate: Entity): IO[Unit] = {
    for {
      _ <- collection
        .update(ordered = false)
        .one(query, toCreate, upsert = true)
        .purifyIn[IO]
        .void
        .recoverWith {
          case e: LastError =>
            MongoCollection.interpretWriteResult(e)

          case NonFatal(e) =>
            IO.raiseError(e)
        }
    } yield ()
  }

  def createOrUpdate(toCreate: Entity): IO[Unit] = {
    for {
      _ <- collection
        .update(ordered = false)
        .one(idQueryByEntity(toCreate), toCreate, upsert = true)
        .purifyIn[IO]
        .void
        .recoverWith {
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

  def remove(q: BSONDocument): IO[Unit] = {
    for {
      _ <- collection.delete().element(q).purifyIn[IO].void.recoverWith {
        case e: LastError =>
          MongoCollection.interpretWriteResult(e)

        case NonFatal(e) =>
          IO.raiseError(e)
      }
    } yield ()
  }

  def remove(id: IdType): IO[Unit] = {
    this.remove(idQuery(id))
  }

}
