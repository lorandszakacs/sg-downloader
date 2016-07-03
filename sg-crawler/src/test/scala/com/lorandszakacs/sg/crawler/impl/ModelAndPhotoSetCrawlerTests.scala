package com.lorandszakacs.sg.crawler.impl

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpRequest
import com.lorandszakacs.sg.crawler.{ModelAndPhotoSetCrawler, PageCrawlerAssembly}
import com.lorandszakacs.sg.crawler.page.PageCrawlerTest
import com.lorandszakacs.sg.http.SGClientAssembly
import com.lorandszakacs.sg.model._
import org.joda.time.LocalDate
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
    whenReady(crawler.gatherPhotoSetInformationFor("odina")) { sets: List[PhotoSet] =>

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
    whenReady(crawler.gatherPhotoSetInformationFor("zoli")) { uris: List[PhotoSet] =>

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
    whenReady(crawler.gatherSGNames(48)) { names: List[String] =>
      withClue("... size") {
        names should have size 48
      }

      withClue("... content") {
        names should contain("Sash")
        names should contain("Kemper")
        names should contain("Gogo")
      }

    }
  }

  //===============================================================================================
  //===============================================================================================

  it should "... gather the first 48 Hopeful names by followers" in { crawler =>
    whenReady(crawler.gatherHopefulNames(48)) { names: List[String] =>

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

  override type FixtureParam = ModelAndPhotoSetCrawler

  override protected def withFixture(test: OneArgTest): Outcome = {
    val assembly = new PageCrawlerAssembly with SGClientAssembly {
      override implicit def actorSystem: ActorSystem = ModelAndPhotoSetCrawlerTests.this.actorSystem

      override implicit def executionContext: ExecutionContext = ModelAndPhotoSetCrawlerTests.this.ec

      override def authentication: (HttpRequest) => HttpRequest = identity
    }

    test.apply(assembly.modelAndSetCrawler)
  }
}
