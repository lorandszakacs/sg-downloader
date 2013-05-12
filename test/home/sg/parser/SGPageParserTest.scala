package home.sg.parser
import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.io.File
import scala.io.Source
import home.sg.util.TestDataResolver

@RunWith(classOf[JUnitRunner])
class SGPageParserTest extends FunSuite {

  private def getTestSourceFile(fileName: String) = {
    val filePath = TestDataResolver.getTestDataFolderForClass(classOf[SGPageParserTest]) + fileName
    scala.io.Source.fromFile(new File(filePath))
  }

  test("Nahp Set-Album Page, 12 total sets") {
    val sgName = "Nahp"
    val nahpSource = getTestSourceFile(TestData.nahpSetAlbumPage)
    val result = SGPageParser.parseSetAlbumPageToSetHeaders(sgName, nahpSource);
    assert(result.length === 12)
  }

  test("Sash Set-Album Page, 30 total sets") {
    val sgName = "Sash"
    val sashSource = getTestSourceFile(TestData.sashSetAlbumPage)
    val result = SGPageParser.parseSetAlbumPageToSetHeaders(sgName, sashSource)
    assert(result.length == 30)
  }

  test("Nahp set page - Girl Next door") {
    val sgName = "Nahp"
    val nahpSource = getTestSourceFile(TestData.NahpGirlNextDoor.testPageName)
    assert(TestData.NahpGirlNextDoor.expectedImageURLs === SGPageParser.parseSetPageToImageURLs(nahpSource))

  }
}

private object TestData {

  //contains only pink sets
  //12 total
  val nahpSetAlbumPage = "set-album-page-nahp.html"

  //contains both pink and mr sets
  //27 pink
  //3 in MR
  val sashSetAlbumPage = "set-album-page-sash.html"

  object NahpGirlNextDoor {
    val testPageName = "set-nahp-girl-next-door.html"
    val expectedImageURLs = List("http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/01.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/02.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/03.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/04.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/05.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/06.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/07.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/08.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/09.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/10.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/11.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/12.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/13.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/14.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/15.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/16.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/17.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/18.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/19.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/20.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/21.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/22.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/23.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/24.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/25.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/26.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/27.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/28.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/29.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/30.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/31.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/32.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/33.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/34.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/35.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/36.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/37.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/38.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/39.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/40.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/41.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/42.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/43.jpg",
      "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/44.jpg")
  }
}

