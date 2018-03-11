package com.lorandszakacs.util.mongodb

import com.lorandszakacs.util.effects._
import com.lorandszakacs.util.math.Identifier
import com.typesafe.scalalogging.LazyLogging
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
    enH: BSONDocumentHandler[Entity],
    idH: BSONHandler[BSONTargetType, IdType],
    sch: DBIOScheduler,
    id:  Identifier[Entity, IdType]
  ): MongoCollection[Entity, IdType, BSONTargetType] = {
    new MongoCollection[Entity, IdType, BSONTargetType] {
      override protected implicit val scheduler:     DBIOScheduler                       = sch
      override protected implicit val entityHandler: BSONDocumentHandler[Entity]         = enH
      override protected implicit val idHandler:     BSONHandler[BSONTargetType, IdType] = idH
      override protected implicit val identifier:    Identifier[Entity, IdType]          = id

      override val collectionName: String = collName

      override protected val db: Database = database
    }
  }

  private def interpretWriteResult(wr: WriteResult): Task[Unit] = {
    wr.ok.failOnFalseTaskThr(
      MongoDBException(
        code = wr.code.map(_.toString),
        msg  = wr.writeErrors.headOption.map(_.toString)
      )
    )
  }

  private def interpretWriteResult(wr: MultiBulkWriteResult): Task[Unit] = {
    for {
      _ <- wr.ok failOnFalseTaskThr MongoDBException(
        code = wr.code.map(_.toString),
        msg  = wr.writeErrors.headOption.map(_.toString)
      )
      _ <- wr.writeErrors.nonEmpty failOnTrueTaskThr MongoDBException(
        code = wr.code.map(_.toString),
        msg  = wr.writeErrors.headOption.map(_.toString)
      )
    } yield ()

  }
}

trait MongoCollection[Entity, IdType, BSONTargetType <: BSONValue] extends LazyLogging {
  protected implicit def scheduler: DBIOScheduler

  protected implicit def entityHandler: BSONDocumentHandler[Entity]

  protected implicit def idHandler: BSONHandler[BSONTargetType, IdType]

  protected implicit def identifier: Identifier[Entity, IdType]

  protected def db: Database

  def collectionName: String

  private lazy val collectionTask: Task[BSONCollection] = db.collection(collectionName)

  private lazy val collection: BSONCollection = collectionTask.unsafeSyncGet()

  def idQuery(id: IdType): BSONDocument = BSONDocument(_id -> id)

  def idQueryByEntity(id: Entity): BSONDocument = BSONDocument(_id -> identifier.id(id))

  def findOne(query: BSONDocument): Task[Option[Entity]] = {
    collection.find(query).one[Entity].suspendInTask
  }

  def findMany(query: BSONDocument, maxDocs: Int = Int.MaxValue): Task[List[Entity]] = {
    val cursor: Cursor[Entity] = collection.find(query).cursor[Entity]()
    cursor.collect[List](maxDocs = maxDocs, err = Cursor.FailOnError[List[Entity]]()).suspendInTask
  }

  def findAll: Task[List[Entity]] = {
    this.findMany(BSONDocument.empty)
  }

  def find(id: IdType): Task[Option[Entity]] = {
    this.findOne(idQuery(id))
  }

  def findManyById(ids: Seq[IdType]): Task[List[Entity]] = {
    if (ids.isEmpty) {
      Task.pure(Nil)
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

  def create(toCreate: Entity): Task[Unit] = {
    for {
      _ <- collection.insert(toCreate).suspendInTask.discardContent.recoverWith {
        case e: LastError =>
          MongoCollection.interpretWriteResult(e)

        case NonFatal(e) =>
          Task.raiseError(e)
      }
    } yield ()
  }

  def create(toCreate: List[Entity]): Task[Unit] = {
    for {
      wr <- collection.insert[Entity](ordered = false).many(toCreate).suspendInTask
      _  <- MongoCollection.interpretWriteResult(wr)
    } yield ()
  }

  def createOrUpdate(query: BSONDocument, toCreate: Entity): Task[Unit] = {
    for {
      _ <- collection.update(query, toCreate, upsert = true).suspendInTask.discardContent.recoverWith {
        case e: LastError =>
          MongoCollection.interpretWriteResult(e)

        case NonFatal(e) =>
          Task.raiseError(e)
      }
    } yield ()
  }

  def createOrUpdate(toCreate: Entity): Task[Unit] = {
    for {
      _ <- collection
        .update(idQueryByEntity(toCreate), toCreate, upsert = true)
        .suspendInTask
        .discardContent
        .recoverWith {
          case e: LastError =>
            MongoCollection.interpretWriteResult(e)

          case NonFatal(e) =>
            Task.raiseError(e)
        }
    } yield ()
  }

  def createOrUpdate(toCreateOrUpdate: List[Entity]): Task[Unit] = {
    for {
      _ <- toCreateOrUpdate.traverse[Task, Unit] { entity: Entity =>
        this.createOrUpdate(entity)
      }
    } yield ()
  }

  def remove(q: BSONDocument, firstMatchOnly: Boolean = false): Task[Unit] = {
    for {
      _ <- collection.remove(q, firstMatchOnly = firstMatchOnly).suspendInTask.discardContent.recoverWith {
        case e: LastError =>
          MongoCollection.interpretWriteResult(e)

        case NonFatal(e) =>
          Task.raiseError(e)
      }
    } yield ()
  }

  def remove(id: IdType): Task[Unit] = {
    this.remove(idQuery(id), firstMatchOnly = true)
  }

}
