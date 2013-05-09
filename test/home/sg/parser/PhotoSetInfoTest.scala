package home.sg.parser

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PhotoSetInfoTest extends FunSuite {

  private val sashMRSet = (
    "Sash",
    "<div class=\"preview\" id=\"album_277610\" title=\"Sash: The Grove\">",
    "<a class=\"pngSpank\" href=\"/girls/Sash/albums/site/33360/\"><img src=\"/media/albums/0/36/33360/setpreview_medium.jpg\" width=\"169\" height=\"104\" alt=\"SuicideGirls: The Grove\" /></a>",
    "<div class=\"date\">Apr 10, 2013</div>")

  private object sashMRSetExpectedValues {
    val expectedDate = "2013.04"
    val expectedTitle = "The Grove"
    val expectedTitleForURI = "/media/albums/0/36/33360/"
    val expectedMRFlag = true
    
    val expectedAlbumSaveLocation = "Sash/2013.04 - The Grove"
    val expectedURI01 = "http://img.suicidegirls.com/media/albums/0/36/33360/1505987.jpg"
    val expectedFile01 = "Sash/2013.03 - The Grove/01.jpg"

    val expectedURI23 = "http://img.suicidegirls.com/media/albums/0/36/33360/1506015.jpg"
    val expectedFile23 = "Sash/2013.03 - The Grove/23.jpg"
  }

  private val sashPinkSet = (
    "Sash",
    "<div class=\"preview\" id=\"album_273231\" title=\"Sash: Arboraceous\">",
    " <a class=\"pngSpank\" href=\"/girls/Sash/photos/Arboraceous/\"><img src=\"/media/girls/Sash/photos/Arboraceous/setpreview_medium.jpg\" ... /></a>",
    "<div class=\"date\">Mar 6, 2013</div>")

  private object sashPinkSetExpectedValues {
    val expectedDate = "2013.03"
    val expectedTitle = "Arboraceous"
    val expectedTitleForURI = "/girls/Sash/photos/Arboraceous/"
    val expectedMRFlag = false

    val expectedAlbumSaveLocation = "Sash/2013.03 - Arboraceous"
    val expectedURI01 = "http://img.suicidegirls.com/media/girls/Sash/photos/Arboraceous/01.jpg"
    val expectedFile01 = "Sash/2013.03 - Arboraceous/01.jpg"

    val expectedURI23 = "http://img.suicidegirls.com/media/girls/Sash/photos/Arboraceous/23.jpg"
    val expectedFile23 = "Sash/2013.03 - Arboraceous/23.jpg"

  }

  private val nahpPinkSetWithSpaces = (
    "Nahp",
    "<div class=\"preview\" id=\"album_271379\" title=\"Nahp:   Girl Next Door\">",
    "<a class=\"pngSpank\" href=\"/girls/Nahp/photos/++Girl+Next+Door/\"><img src=\"/media/girls/Nahp/photos/  Girl Next Door/setpreview_medium.jpg\" width=\"169\" height=\"104\" alt=\"SuicideGirls:   Girl Next Door\" /></a>",
    "<div class=\"date\">Jan 1, 2013</div>")

  private object nahpPinkSetWithSpacesExpectedValues {
    val expectedDate = "2013.01"
    val expectedTitle = "Girl Next Door"
    val expectedTitleForURI = "/girls/Nahp/photos/%20%20Girl%20Next%20Door/"
    val expectedMRFlag = false

    val expectedAlbumSaveLocation = "Nahp/2013.01 - Girl Next Door"
    val expectedURI01 = "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/01.jpg"
    val expectedFile01 = "Nahp/2013.01 - Girl Next Door/01.jpg"
    val expectedURI23 = "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/23.jpg"
    val expectedFile23 = "Nahp/2013.01 - Girl Next Door/23.jpg"

  }

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
    result
  }

  test("MR set, Sash, The Grove") {
    val e = sashMRSetExpectedValues
    assertCorrectValues(sashMRSet, e.expectedDate, e.expectedTitle, e.expectedTitleForURI, e.expectedMRFlag)
  }

  test("pink set, Sash, Arboraceous") {
    val e = sashPinkSetExpectedValues
    assertCorrectValues(sashPinkSet, e.expectedDate, e.expectedTitle, e.expectedTitleForURI, e.expectedMRFlag)
  }

  test("pink set with spaces in title, Nahp, Girl Next Door") {
    val e = nahpPinkSetWithSpacesExpectedValues
    assertCorrectValues(nahpPinkSetWithSpaces, e.expectedDate, e.expectedTitle, e.expectedTitleForURI, e.expectedMRFlag)
  }

  test("pink set with spaces in title for path construction, Nahp, Girl Next Door") {
    val e = nahpPinkSetWithSpacesExpectedValues
    val result = assertCorrectValues(nahpPinkSetWithSpaces, e.expectedDate, e.expectedTitle, e.expectedTitleForURI, e.expectedMRFlag)
    assert(result.relativeAlbumSaveLocation === e.expectedAlbumSaveLocation)

    result.imageDownloadAndSaveLocationPairs match {
      case Some(pairs) => {
        assert(pairs.head === (e.expectedURI01, e.expectedFile01))
        assert(pairs(22) === (e.expectedURI23, e.expectedFile23))
      }
      case None => fail("Should have computed image URIs for pink set")
    }
  }

  test("MR set with spaces in title for path construction, Sash, The Grove") {
   val e = sashMRSetExpectedValues
    val result = assertCorrectValues(sashMRSet, e.expectedDate, e.expectedTitle, e.expectedTitleForURI, e.expectedMRFlag)
    assert(result.relativeAlbumSaveLocation === e.expectedAlbumSaveLocation)

    result.imageDownloadAndSaveLocationPairs match {
      case Some(pairs) => fail("Should NOT have computed image URIs for member review set")
      case None => assert(true)
    }
  }

}