package com.lorandszakacs.sg.crawler.impl

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpRequest
import com.lorandszakacs.sg.crawler.{ModelAndPhotoSetCrawler, PageCrawlerAssembly}
import com.lorandszakacs.sg.http.SGClientAssembly
import com.lorandszakacs.sg.model._
import org.joda.time.{DateTime, LocalDate}
import org.joda.time.format.DateTimeFormat
import org.scalatest.Outcome

import scala.concurrent.ExecutionContext

/**
  *
  * This test requires an active internet connection,
  * since it fetches live data!
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 03 Jul 2016
  *
  */
class ModelAndPhotoSetCrawlerTests extends PageCrawlerTest {

  //===============================================================================================
  //===============================================================================================

  behavior of "GirlAndPhotoSetCrawler.getPhotoSetUris"

  //===============================================================================================
  //===============================================================================================

  /**
    * at the time of writing of this test:
    * https://www.suicidegirls.com/members/odina/photos/
    * had only one single set
    */
  it should "... fetch URIs for a page that does not need a subsequent query -- odina" in { crawler =>
    whenReady(crawler.gatherPhotoSetInformationFor(ModelName("odina"))) { sets: List[PhotoSet] =>

      withClue("size") {
        sets should have size 1
      }

      withClue("content") {
        sets should contain {
          PhotoSet(
            url = "https://www.suicidegirls.com/members/odina/album/2745718/do-i-wanna-know/",
            title = "DO I WANNA KNOW",
            date = LocalDate.parse("2016-07-03")
          )
        }
      }

    }
  }

  /**
    * at the time of writing of this test:
    * https://www.suicidegirls.com/girls/zoli/photos/
    * had 22 sets. And has not published a new set in ages.
    */
  it should "... fetch URIs for a page that needs several queries -- zoli" in { crawler =>
    whenReady(crawler.gatherPhotoSetInformationFor(ModelName("zoli"))) { uris: List[PhotoSet] =>

      withClue("... size") {
        uris should have size 22
      }

      withClue("... content") {
        uris should contain {
          PhotoSet(
            url = "https://www.suicidegirls.com/girls/zoli/album/996153/lounge-act/",
            title = "lounge act",
            date = LocalDate.parse("2012-10-17")
          )
        }

        uris should contain {
          PhotoSet(
            url = "https://www.suicidegirls.com/girls/zoli/album/969351/the-beat/",
            title = "THE BEAT",
            date = LocalDate.parse("2006-05-03")
          )
        }
      }

    }
  }

  //===============================================================================================
  //===============================================================================================

  behavior of "GirlAndPhotoSetCrawler.gatherSGNames"

  //===============================================================================================
  //===============================================================================================

  /**
    * It's important to keep in mind that since this is live data that is being fetched,
    * this test might fail. Therefore one must always be vigilant.
    */
  it should "... gather the first 48 SG names by followers" in { crawler =>
    whenReady(crawler.gatherSGNames(48)) { names: List[ModelName] =>
      withClue("... size") {
        names should have size 48
      }

      withClue("... content") {
        names should contain("Sash".toModelName)
        names should contain("Kemper".toModelName)
        names should contain("Gogo".toModelName)
      }

    }
  }

  //===============================================================================================
  //===============================================================================================

  it should "... gather the first 48 Hopeful names by followers" in { crawler =>
    whenReady(crawler.gatherHopefulNames(48)) { names: List[ModelName] =>

      println {
        s"""
           |hopeful names:
           |${names.mkString("\n")}
           |
        """.stripMargin
      }

      withClue("... size") {
        names should have size 48
      }
    }
  }

  //===============================================================================================
  //===============================================================================================

  behavior of "GirlAndPhotoSetCrawler.gatherNewestModelInformation"

  //===============================================================================================
  //===============================================================================================

  it should "... gather the first 48 new sets" in { crawler =>
    whenReady(crawler.gatherNewestModelInformation(48, None)) { models: List[Model] =>

      withClue("... size") {
        models should have size 48
      }
      withClue("... distribution") {
        assert(models.exists(_.isHopeful), "... there should be at least one hopeful in the past 48 new sets")
        assert(models.exists(_.isSuicideGirl), "... there should be at least one suicidegirl in the past 48 new sets")
      }
    }
  }

  //===============================================================================================
  //===============================================================================================

  it should "... gather the first 48 new sets, then use one in the middle as the latest processed, and return only the ones before it" in { crawler =>
    val previousModels = whenReady(crawler.gatherNewestModelInformation(48, None)) { models: List[Model] =>
      withClue("... size") {
        models should have size 48
      }
      withClue("... distribution") {
        assert(models.exists(_.isHopeful), "... there should be at least one hopeful in the past 48 new sets")
        assert(models.exists(_.isSuicideGirl), "... there should be at least one suicidegirl in the past 48 new sets")
      }
      models
    }
    val index = 13
    val latest = previousModels(index)
    val lastProcessed: LastProcessedMarker = crawler.createLastProcessedIndex(latest)

    withClue("... now gathering only a part of the processed sets") {
      whenReady(crawler.gatherNewestModelInformation(48, Option(lastProcessed))) { models: List[Model] =>
        withClue("... size") {
          models should have size index
        }
        withClue("... distribution") {
          assert(!models.exists(_.name == latest.name), "... the latest model should not be in this list")
          assert(models.exists(_.isHopeful), "... there should be at least one hopeful in the past 48 new sets")
          assert(models.exists(_.isSuicideGirl), "... there should be at least one suicidegirl in the past 48 new sets")
        }
        models
      }
    }
  }

  //===============================================================================================
  //===============================================================================================

  it should "... gather the first 48 new sets, then use first one as latest. No subsequent models should be returned" in { crawler =>
    val previousModels = whenReady(crawler.gatherNewestModelInformation(48, None)) { models: List[Model] =>
      withClue("... size") {
        models should have size 48
      }
      withClue("... distribution") {
        assert(models.exists(_.isHopeful), "... there should be at least one hopeful in the past 48 new sets")
        assert(models.exists(_.isSuicideGirl), "... there should be at least one suicidegirl in the past 48 new sets")
      }
      models
    }
    val index = 0
    val latest = previousModels(index)
    val lastProcessed: LastProcessedMarker = crawler.createLastProcessedIndex(latest)

    withClue("... now gathering only a part of the processed sets") {
      whenReady(crawler.gatherNewestModelInformation(48, Option(lastProcessed))) { models: List[Model] =>
        withClue("... size") {
          models should have size index
        }
        withClue("... distribution") {
          assert(!models.exists(_.name == latest.name), "... the latest model should not be in this list")
        }
        models
      }
    }
  }

  //===============================================================================================
  //===============================================================================================

  override type FixtureParam = ModelAndPhotoSetCrawler

  override protected def withFixture(test: OneArgTest): Outcome = {
    val assembly = new PageCrawlerAssembly with SGClientAssembly {
      override implicit def actorSystem: ActorSystem = ModelAndPhotoSetCrawlerTests.this.actorSystem

      override implicit def executionContext: ExecutionContext = ModelAndPhotoSetCrawlerTests.this.ec

    }

    test.apply(assembly.modelAndSetCrawler)
  }
}
