package com.lorandszakacs.sg.crawler.page


import akka.http.scaladsl.model.Uri
import com.lorandszakacs.sg.http.SGClient
import com.lorandszakacs.sg.model._
import org.joda.time.LocalDate
import org.scalatest.Outcome

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
    whenReady(crawler.getPhotoSetInformation("odina")) { sets: List[PhotoSet] =>

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
    whenReady(crawler.getPhotoSetInformation("zoli")) { uris: List[PhotoSet] =>

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

  override type FixtureParam = GirlAndPhotoSetCrawler

  override protected def withFixture(test: OneArgTest): Outcome = {
    val client = SGClient()
    val crawler = new GirlAndPhotoSetCrawler(client)
    test.apply(crawler)
  }
}