package home.sg.parser

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import home.sg.parser.html.PhotoSetHeader

@RunWith(classOf[JUnitRunner])
class PhotoSetHeaderTest extends FunSuite {

  private val sashMRSetRaw = (
    "Sash",
    "<div class=\"preview\" id=\"album_277610\" title=\"Sash: The Grove\">",
    "<a class=\"pngSpank\" href=\"/members/Sash/albums/site/33360/\"/><img src=\"/media/albums/0/36/33360/setpreview_medium.jpg\" width=\"169\" height=\"104\" alt=\"SuicideGirls: The Grove\" title=\"Sash: The Grove\" /></a>",
    "<div class=\"date\">Apr 10, 2013</div>")

  private abstract class ExpectedValues(val initialData: (String, String, String, String), val date: String, val title: String, val URL: String, val saveLocation: String)

  private object sashMRSet extends ExpectedValues(
    sashMRSetRaw,
    "2013.04",
    "The Grove",
    "http://suicidegirls.com/members/Sash/albums/site/33360/",
    "Sash/2013.04 - The Grove")

  private val sashPinkSetRaw = (
    "Sash",
    "<div class=\"preview\" id=\"album_273231\" title=\"Sash: Arboraceous\">",
    " <a class=\"pngSpank\" href=\"/girls/Sash/photos/Arboraceous/\"><img src=\"/media/girls/Sash/photos/Arboraceous/setpreview_medium.jpg\" ... /></a>",
    "<div class=\"date\">Mar 6, 2013</div>")

  private object sashPinkSet extends ExpectedValues(
    sashPinkSetRaw,
    "2013.03",
    "Arboraceous",
    "http://suicidegirls.com/girls/Sash/photos/Arboraceous/",
    "Sash/2013.03 - Arboraceous")

  private val nahpPinkSetWithSpacesRaw = (
    "Nahp",
    "<div class=\"preview\" id=\"album_271379\" title=\"Nahp:   Girl Next Door\">",
    "<a class=\"pngSpank\" href=\"/girls/Nahp/photos/++Girl+Next+Door/\"><img src=\"/media/girls/Nahp/photos/  Girl Next Door/setpreview_medium.jpg\" width=\"169\" height=\"104\" alt=\"SuicideGirls:   Girl Next Door\" /></a>",
    "<div class=\"date\">Jan 1, 2013</div>")

  private object nahpPinkSetWithSpaces extends ExpectedValues(
    nahpPinkSetWithSpacesRaw,
    "2013.01",
    "Girl Next Door",
    "http://suicidegirls.com/girls/Nahp/photos/++Girl+Next+Door/",
    "Nahp/2013.01 - Girl Next Door")

  private def assertCorrectValues(expected: ExpectedValues) = {
    val sgName = expected.initialData._1
    val preview = expected.initialData._2
    val pngSpank = expected.initialData._3
    val date = expected.initialData._4

    val result = new PhotoSetHeader(sgName, preview, pngSpank, date);
    assert(result.sgName === sgName, "SG name mismatch")
    assert(result.date === expected.date, "date mismatch")
    assert(result.title === expected.title, "title mismatch")
    assert(result.URL === expected.URL, "URL mismatch")
    result
  }

  test("MR set, Sash, The Grove") {
    val e = sashMRSet
    assertCorrectValues(sashMRSet)
  }

  test("pink set, Sash, Arboraceous") {
    val e = sashPinkSet
    assertCorrectValues(sashPinkSet)
  }

  test("pink set with spaces in title, Nahp, Girl Next Door") {
    val e = nahpPinkSetWithSpaces
    assertCorrectValues(nahpPinkSetWithSpaces)
  }

  // test("print shit") {
  //    val e = nahpPinkSetWithSpaces
  //
  //    val result = new PhotoSetInfo(e._1, e._2, e._3, e._4).imageDownloadAndSaveLocationPairs.get
  //    result map { p => println("\"" + "(" + p._1 + "\"" + "," + "\"" + p._2 + ")" + "\",") }
  //  }

}