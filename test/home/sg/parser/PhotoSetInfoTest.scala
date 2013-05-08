package home.sg.parser

import org.scalatest.FunSuite

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PhotoSetInfoTest extends FunSuite {

  val mrSet = (
    "Sash",
    "<div class=\"preview\" id=\"album_277610\" title=\"Sash: The Grove\">",
    "<a class=\"pngSpank\" href=\"/girls/Sash/albums/site/33360/\"><img src=\"/media/albums/0/36/33360/setpreview_medium.jpg\" width=\"169\" height=\"104\" alt=\"SuicideGirls: The Grove\" /></a>",
    "<div class=\"date\">Apr 10, 2013</div>")

  val pinkSet = (
    "Sash",
    "<div class=\"preview\" id=\"album_273231\" title=\"Sash: Arboraceous\">",
    " <a class=\"pngSpank\" href=\"/girls/Sash/photos/Arboraceous/\"><img src=\"/media/girls/Sash/photos/Arboraceous/setpreview_medium.jpg\" ... /></a>",
    "<div class=\"date\">Mar 6, 2013</div>")

  val pinkSetWithSpaces = (
    "Nahp",
    "<div class=\"preview\" id=\"album_271379\" title=\"Nahp:   Girl Next Door\">",
    "<a class=\"pngSpank\" href=\"/girls/Nahp/photos/++Girl+Next+Door/\"><img src=\"/media/girls/Nahp/photos/  Girl Next Door/setpreview_medium.jpg\" width=\"169\" height=\"104\" alt=\"SuicideGirls:   Girl Next Door\" /></a>",
    "<div class=\"date\">Jan 1, 2013</div>")

  private def assertCorrectValues(set: (String, String, String, String), expectedDate: String, expectedTitle: String, expectedTitleForURI: String, expectedFlag: Boolean) = {
    val sgName = set._1
    val preview = set._2
    val pngSpank = set._3
    val date = set._4

    val result = new PhotoSetInfo(sgName, preview, pngSpank, date);
    assert(result.sgName === sgName, "SG name mismatch")
    assert(result.date === expectedDate, "date mismatch")
    assert(result.setTitle === expectedTitle, "title mismatch")
    assert(result.setTitleAsURIPath === expectedTitleForURI, "tileForURI mismatch")
    assert(result.isMR === expectedFlag, "MR flag mismatch")
  }

  test("MR set, Sash, The Grove") {
    val expectedDate = "2013.04"
    val expectedTitle = "The Grove"
    val expectedTitleForURI = "/media/albums/0/36/33360/"
    val expectedMRFlag = true
    assertCorrectValues(mrSet, expectedDate, expectedTitle, expectedTitleForURI, expectedMRFlag)
  }

  test("pink set, Sash, Arboraceous") {
    val expectedDate = "2013.03"
    val expectedTitle = "Arboraceous"
    val expectedTitleForURI = "/girls/Sash/photos/Arboraceous/"
    val expectedMRFlag = false
    assertCorrectValues(pinkSet, expectedDate, expectedTitle, expectedTitleForURI, expectedMRFlag)
  }

  test("pink set with spaces in title, Nahp, Girl Next Door") {
    val expectedDate = "2013.01"
    val expectedTitle = "Girl Next Door"
    val expectedTitleForURI = "/girls/Nahp/photos/%20%20Girl%20Next%20Door/"
    val expectedMRFlag = false
    assertCorrectValues(pinkSetWithSpaces, expectedDate, expectedTitle, expectedTitleForURI, expectedMRFlag)
  }

}