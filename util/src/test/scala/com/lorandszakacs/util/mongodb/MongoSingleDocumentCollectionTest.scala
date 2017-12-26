package com.lorandszakacs.util.mongodb

import com.lorandszakacs.util.future._
import org.scalatest.{fixture, Matchers, OneInstancePerTest, Outcome}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 15 Jul 2017
  *
  */
class MongoSingleDocumentCollectionTest extends fixture.FlatSpec with OneInstancePerTest with Matchers {
  private implicit val ec: ExecutionContext = ExecutionContext.global

  class SingleEntityRepository(
    override protected val db: Database
  )(implicit
    override protected implicit val executionContext: ExecutionContext)
      extends SingleDocumentMongoCollection[Entity, String, BSONString] {
    protected implicit def objectHandler: BSONDocumentHandler[Entity] = BSONMacros.handler[Entity]

    override protected implicit lazy val idHandler: BSONHandler[BSONString, String] =
      BSONStringHandler

    override def collectionName: String = "test_entities"

    override protected def uniqueDocumentId: String = "a_unique_id"

    override protected def defaultEntity: Entity = Entity(
      None,
      ""
    )
  }

  case class Entity(
    opt:    Option[String],
    actual: String
  )

  override protected def withFixture(test: OneArgTest): Outcome = {
    val dbName = Database.testName(this.getClass.getSimpleName, test.text)
    val db = new Database(
      uri    = """mongodb://localhost:27016""",
      dbName = dbName
    )
    val repo = new SingleEntityRepository(db)(ec)
    db.drop().await()
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
    f.await()
    outcome
  }

  override type FixtureParam = SingleEntityRepository

  //===========================================================================
  //===========================================================================
  behavior of "MongoCollection"

  it should "001 single: write + read + update + remove" in { repo =>
    val e = Entity(
      opt    = Option("MY FIRST VALUE"),
      actual = "ACTUAL"
    )
    withClue("create single doc") {
      repo.create(e).await()
    }

    withClue("read after create") {
      val read = repo.get.await()
      assert(read == e)
    }

    val eu = e.copy(opt = Some("NEW VALUE"))
    withClue("update") {
      repo.createOrUpdate(eu).await()
      val read = repo.get.await()
      assert(eu == read)
    }

    withClue("remove") {
      repo.remove().await()
      val read = repo.find.await()
      assert(read.isEmpty)
    }
  }

  //===========================================================================
  //===========================================================================

  it should "002 fail when double creating it" in { repo =>
    val e = Entity(
      opt    = Option("MY FIRST VALUE"),
      actual = "ACTUAL"
    )
    withClue("create single doc") {
      repo.create(e).await()
    }

    withClue("create second time doc") {
      the[MongoDBException] thrownBy {
        repo.create(e).await()
      }
    }

    withClue("read after create") {
      val read = repo.get.await()
      assert(read == e)
    }
  }
}
