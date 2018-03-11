package com.lorandszakacs.util.mongodb

import com.lorandszakacs.util.effects._
import org.scalatest.{fixture, Matchers, OneInstancePerTest, Outcome}

/**
  *
  * N.B.
  * Known to fail in ``sbt``. But in Intellij, they run. And normal application start does find the configs.
  * So no idea why this happens only during ``sbt test``
  *{{{
  *   [info]   com.typesafe.config.ConfigException$Missing: No configuration setting found for key 'akka'
  *   [info]   at com.typesafe.config.impl.SimpleConfig.findKeyOrNull(SimpleConfig.java:156)
  *   [info]   at com.typesafe.config.impl.SimpleConfig.findKey(SimpleConfig.java:149)
  *   [info]   at com.typesafe.config.impl.SimpleConfig.findOrNull(SimpleConfig.java:176)
  *   [info]   at com.typesafe.config.impl.SimpleConfig.find(SimpleConfig.java:188)
  *   [info]   at com.typesafe.config.impl.SimpleConfig.find(SimpleConfig.java:193)
  *   [info]   at com.typesafe.config.impl.SimpleConfig.getString(SimpleConfig.java:250)
  *   [info]   at akka.actor.ActorSystem$Settings.<init>(ActorSystem.scala:316)
  *   [info]   at akka.actor.ActorSystemImpl.<init>(ActorSystem.scala:667)
  *   [info]   at akka.actor.ActorSystem$.apply(ActorSystem.scala:246)
  *   [info]   at akka.actor.ActorSystem$.apply(ActorSystem.scala:289)
  *   [info]   at reactivemongo.api.Driver.$init$(Driver.scala:68)
  *   [info]   at reactivemongo.api.MongoDriver.<init>(MongoDriver.scala:31)
  *   [info]   at com.lorandszakacs.util.mongodb.Database.$anonfun$mongoDriverTask$1(Database.scala:25)
  *   [info]   at monix.eval.internal.TaskRunLoop$.startFull(TaskRunLoop.scala:107)
  *   [info]   at monix.eval.internal.TaskRunLoop$RestartCallback.onSuccess(TaskRunLoop.scala:659)
  *   [info]   at monix.eval.Task$$anon$3.run(Task.scala:1529)
  *   [info]   at scala.concurrent.impl.ExecutionContextImpl$AdaptedForkJoinTask.exec(ExecutionContextImpl.scala:140)
  *   [info]   at java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:289)
  *   [info]   at java.util.concurrent.ForkJoinPool$WorkQueue.runTask(ForkJoinPool.java:1056)
  *   [info]   at java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1692)
  *   [info]   at java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:157)
  *}}}
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 15 Jul 2017
  *
  */
class MongoSingleDocumentCollectionTest extends fixture.FlatSpec with OneInstancePerTest with Matchers {
  private implicit val dbIOScheduler: DBIOScheduler = DBIOScheduler(Scheduler.global)

  class SingleEntityRepository(override protected val db: Database)(
    implicit
    override protected implicit val dbIOScheduler: DBIOScheduler
  ) extends SingleDocumentMongoCollection[Entity, String, BSONString] {
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
      actual = "ACTUAL"
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
      actual = "ACTUAL"
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
