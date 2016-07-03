package com.lorandszakacs.sg.crawler.page

import akka.http.scaladsl.model.Uri
import com.lorandszakacs.sg.http.SGClient
import org.scalatest.concurrent.{ScalaFutures, Futures}
import org.scalatest.{Outcome, fixture}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 03 Jul 2016
  *
  */
class GirlAndPhotoSetCrawlerTests extends PageCrawlerTest {

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
    whenReady(crawler.getPhotoSetUris("odina")) { uris: List[Uri] =>

      withClue("size") {
        uris should have size 1
      }

      withClue("content") {
        uris should contain(Uri("https://www.suicidegirls.com/members/odina/album/2745718/do-i-wanna-know/"))
      }

    }
  }

  /**
    * at the time of writing of this test:
    * https://www.suicidegirls.com/girls/zoli/photos/
    * had 22 sets. And has not published a new set in ages.
    */
  it should "... fetch URIs for a page that needs several queries -- zoli" in { crawler =>
    whenReady(crawler.getPhotoSetUris("zoli")) { uris: List[Uri] =>

      withClue("... size") {
        uris should have size 22
      }

      withClue("... content") {
        uris should contain(Uri("https://www.suicidegirls.com/girls/zoli/album/996153/lounge-act/"))
        uris should contain(Uri("https://www.suicidegirls.com/girls/zoli/album/969351/the-beat/"))
      }

    }
  }

  //===============================================================================================
  //===============================================================================================

  override type FixtureParam = GirlAndPhotoSetCrawler

  override protected def withFixture(test: OneArgTest): Outcome = {
    val client = SGClient()
    val crawler = new GirlAndPhotoSetCrawler(client)
    test.apply(crawler)
  }
}
