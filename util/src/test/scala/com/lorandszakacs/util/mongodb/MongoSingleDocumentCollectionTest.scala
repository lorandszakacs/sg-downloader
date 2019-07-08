package com.lorandszakacs.util.mongodb

import com.lorandszakacs.util.effects._
import org.scalatest.flatspec.FixtureAnyFlatSpec
import org.scalatest.{Matchers, OneInstancePerTest, Outcome}

/**
  *
  * N.B.
  * Known to fail in ``sbt``. But in Intellij, they run. And normal application start does find the configs.
  * So no idea why this happens only during ``sbt test``
  *{{{
  *   [info]   com.typesafe.config.ConfigException$Missing: No configuration setting found for key 'akka'
  *   ...
  *}}}
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 15 Jul 2017
  *
  */
class MongoSingleDocumentCollectionTest extends FixtureAnyFlatSpec with OneInstancePerTest with Matchers {
  implicit private val dbIOScheduler: DBIOScheduler    = DBIOScheduler(Scheduler.global)
  implicit private val futureLift:    FutureLift[Task] = TaskFutureLift.create

  class SingleEntityRepository(override protected val db: Database)(
    implicit
    implicit override protected val dbIOScheduler: DBIOScheduler,
    implicit override protected val futureLift:    FutureLift[Task],
  ) extends SingleDocumentMongoCollection[Entity, String, BSONString] {
    implicit protected def objectHandler: BSONDocumentHandler[Entity] = BSONMacros.handler[Entity]

    implicit override protected lazy val idHandler: BSONHandler[BSONString, String] =
      BSONStringHandler

    override def collectionName: String = "test_entities"

    override protected def uniqueDocumentId: String = "a_unique_id"

    override protected def defaultEntity: Entity = Entity(
      None,
      "",
    )
  }

  case class Entity(
    opt:    Option[String],
    actual: String,
  )

  override protected def withFixture(test: OneArgTest): Outcome = {
    val dbName = Database.testName(this.getClass.getSimpleName, test.text)
    val db = new Database(
      uri    = """mongodb://localhost:27016""",
      dbName = dbName,
    )
    val repo = new SingleEntityRepository(db)
    db.drop().unsafeSyncGet()
    val outcome: Outcome = withFixture(test.toNoArgTest(repo))
    val f = if (outcome.isFailed || outcome.isCanceled) {
      db.shutdown()
    }
    else {
      for {
        _ <- db.drop()
        _ <- db.shutdown()
      } yield ()
    }
    f.unsafeSyncGet()
    outcome
  }

  override type FixtureParam = SingleEntityRepository

  //===========================================================================
  //===========================================================================
  behavior of "MongoCollection"

  it should "001 single: write + read + update + remove" in { repo =>
    val e = Entity(
      opt    = Option("MY FIRST VALUE"),
      actual = "ACTUAL",
    )
    withClue("create single doc") {
      repo.create(e).unsafeSyncGet()
    }

    withClue("read after create") {
      val read = repo.get.unsafeSyncGet()
      assert(read == e)
    }

    val eu = e.copy(opt = Some("NEW VALUE"))
    withClue("update") {
      repo.createOrUpdate(eu).unsafeSyncGet()
      val read = repo.get.unsafeSyncGet()
      assert(eu == read)
    }

    withClue("remove") {
      repo.remove().unsafeSyncGet()
      val read = repo.find.unsafeSyncGet()
      assert(read.isEmpty)
    }
  }

  //===========================================================================
  //===========================================================================

  it should "002 fail when double creating it" in { repo =>
    val e = Entity(
      opt    = Option("MY FIRST VALUE"),
      actual = "ACTUAL",
    )
    withClue("create single doc") {
      repo.create(e).unsafeSyncGet()
    }

    withClue("create second time doc") {
      the[MongoDBException] thrownBy {
        repo.create(e).unsafeSyncGet()
      }
    }

    withClue("read after create") {
      val read = repo.get.unsafeSyncGet()
      assert(read == e)
    }
  }
}
