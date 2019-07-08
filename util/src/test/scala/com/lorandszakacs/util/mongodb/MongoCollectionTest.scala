package com.lorandszakacs.util.mongodb

import com.lorandszakacs.util.effects._
import com.lorandszakacs.util.math.Identifier
import org.scalatest._
import org.scalatest.flatspec.FixtureAnyFlatSpec

/**
  *
  * N.B.
  * Known to fail in ``sbt``. But in Intellij, they run. And normal application start does find the configs.
  * So no idea why this happens only during ``sbt test``
  *{{{
  *   [info]   com.typesafe.config.ConfigException$Missing: No configuration setting found for key 'akka'
  *   ....
  *}}}
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 15 Jul 2017
  *
  */
class MongoCollectionTest extends FixtureAnyFlatSpec with OneInstancePerTest with Matchers {
  implicit private val dbIOScheduler: DBIOScheduler    = DBIOScheduler(Scheduler.global)
  implicit private val futureLift:    FutureLift[Task] = TaskFutureLift.create

  class EntityRepository(override protected val db: Database)(
    implicit
    implicit override protected val dbIOScheduler: DBIOScheduler,
    implicit override protected val futureLift:    FutureLift[Task],
  ) extends MongoCollection[Entity, String, BSONString] {
    implicit protected def entityHandler: BSONDocumentHandler[Entity] = BSONMacros.handler[Entity]

    implicit override protected lazy val idHandler: BSONHandler[BSONString, String] =
      BSONStringHandler

    implicit override protected lazy val identifier: Identifier[Entity, String] = Identifier { e =>
      e.id
    }

    override def collectionName: String = "test_entities"
  }

  case class Entity(
    @Annotations.Key("_id") id: String,
    opt:                        Option[String],
    actual:                     String,
  )

  override protected def withFixture(test: OneArgTest): Outcome = {
    val dbName = Database.testName(this.getClass.getSimpleName, test.text)
    val db = new Database(
      uri    = """mongodb://localhost:27016""",
      dbName = dbName,
    )
    val repo = new EntityRepository(db)
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

  override type FixtureParam = EntityRepository

  //===========================================================================
  //===========================================================================
  behavior of "MongoCollection"

  it should "001 single: write + read + update + remove" in { repo =>
    val original = Entity(
      id     = "1",
      opt    = Some("OPTIONAL"),
      actual = "VALUE",
    )

    withClue("we create") {
      repo.create(original).unsafeSyncGet()
    }

    withClue("we read") {
      val read = repo
        .find(original.id)
        .unsafeSyncGet()
        .getOrElse(
          fail("... expected entity"),
        )
      assert(read == original)
    }

    val updateNoOpt = original.copy(opt = None)

    withClue("we createOrUpdate") {
      repo.createOrUpdate(updateNoOpt).unsafeSyncGet()
    }

    withClue("we read after update") {
      val read = repo
        .find(updateNoOpt.id)
        .unsafeSyncGet()
        .getOrElse(
          fail("... expected entity"),
        )
      assert(read == updateNoOpt)
    }

    withClue("we remove") {
      repo.remove(original.id).unsafeSyncGet()
    }

    withClue("we read after remove") {
      val read = repo.find(original.id).unsafeSyncGet()
      assert(read.isEmpty)
    }
  }

  //===========================================================================
  //===========================================================================

  it should "002: attempt to write twice" in { repo =>
    val original = Entity(
      id     = "1",
      opt    = Some("OPTIONAL"),
      actual = "VALUE",
    )

    withClue("we create") {
      repo.create(original).unsafeSyncGet()
    }

    withClue("we create... again, should get exception") {
      the[MongoDBException] thrownBy {
        repo.create(original).unsafeSyncGet()
      }
    }
  }

  //===========================================================================
  //===========================================================================

  it should "003: bulk create + many read + bulk update" in { repo =>
    val e1 = Entity(
      id     = "1",
      opt    = Some("OPTIONAL"),
      actual = "VALUE",
    )

    val e2 = Entity(
      id     = "2",
      opt    = Some("OPTIONAL2"),
      actual = "VALUE2",
    )

    val e3 = Entity(
      id     = "3",
      opt    = Some("OPTIONAL3"),
      actual = "VALUE3",
    )

    withClue("we create all") {
      repo.create(List(e1, e2, e3)).unsafeSyncGet()
    }

    withClue("we get all") {
      val all = repo.findAll.unsafeSyncGet()
      assert(List(e1, e2, e3) === all.sortBy(_.id))
    }

    withClue("we get only ones with IDS 1, 2 -- query") {
      val q = document(
        _id -> document(
          $in -> array(e1.id, e2.id),
        ),
      )
      val found = repo.findMany(q).unsafeSyncGet()
      assert(List(e1, e2) == found.sortBy(_.id))
    }

    withClue("we get only ones with IDS 1, 2 -- id method") {
      val found = repo.findManyById(List(e1.id, e2.id)).unsafeSyncGet()
      assert(List(e1, e2) == found.sortBy(_.id))
    }

    val e1U = e1.copy(opt = None)
    val e2U = e2.copy(opt = None)
    withClue("bulk update two") {
      repo.createOrUpdate(List(e1U, e2U)).unsafeSyncGet()
    }

    withClue("get all after bulk update of two") {
      val all = repo.findAll.unsafeSyncGet()
      assert(List(e1U, e2U, e3) == all.sortBy(_.id))
    }
  }
  //===========================================================================
  //===========================================================================

  it should "004: bulk create + double create" in { repo =>
    val e1 = Entity(
      id     = "1",
      opt    = Some("OPTIONAL"),
      actual = "VALUE",
    )

    val e2 = Entity(
      id     = "2",
      opt    = Some("OPTIONAL2"),
      actual = "VALUE2",
    )

    val e3 = Entity(
      id     = "3",
      opt    = Some("OPTIONAL3"),
      actual = "VALUE3",
    )

    withClue("we create all") {
      repo.create(List(e1, e2, e3)).unsafeSyncGet()
    }

    withClue("we get all") {
      val all = repo.findAll.unsafeSyncGet()
      assert(List(e1, e2, e3) === all.sortBy(_.id))
    }

    val e1U = e1.copy(opt = None)
    val e2U = e2.copy(opt = None)
    withClue("bulk update two") {
      the[MongoDBException] thrownBy {
        repo.create(List(e1U, e2U)).unsafeSyncGet()
      }

    }

    withClue("get all after failed bulk update, they should be ok") {
      val all = repo.findAll.unsafeSyncGet()
      assert(List(e1, e2, e3) == all.sortBy(_.id))
    }
  }
}
