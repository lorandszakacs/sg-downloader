package home.sg.parser.html

import java.io.File
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import home.sg.util.TestDataResolver
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SGPageParserTest extends FunSuite {

  private def getTestSourceFile(fileName: String): List[String] = {
    val filePath = TestDataResolver.getTestDataFolderForClass(classOf[SGPageParserTest]) + fileName
    scala.io.Source.fromFile(new File(filePath)).getLines.mkString("123456789").split("123456789").toList
  }

  private def testSetHeaderParsing(sg: SG) = {
    val setAlbumPage = getTestSourceFile(sg.testSetAlbumPageName)
    val result = SGPageParser.parseSetAlbumPageToSetHeaders(sg.name, setAlbumPage)
    assert(result.length === sg.numberOfExpectedSets,
      "Number of sets expected did not equal number of sets parsed. Expected: %s".format(sg.numberOfExpectedSets))
    result
  }

  private def testSetHeader(expected: Set, actual: PhotoSetHeader) = {
    assert(actual.date === expected.date)
    assert(actual.title === expected.title)
  }

  test("Nahp Set-Album Page, 12 total sets") {
    val nahp = TestData.Nahp
    val result = testSetHeaderParsing(nahp)

    val resultGirlNextDoor = result.head
    testSetHeader(nahp.GirlNextDoor, resultGirlNextDoor)
  }

  test("Sash Set-Album Page, 30 total sets") {
    val sash = TestData.Sash
    val result = testSetHeaderParsing(sash)

    val resultTheGrove = result.head
    testSetHeader(sash.TheGrove, resultTheGrove)
    val resultArboraceous = result.tail.head
    testSetHeader(sash.Arboraceaous, resultArboraceous)
  }

  test("Nahp set page - Girl Next door") {
    val nahp = TestData.Nahp
    val nahpSource = getTestSourceFile(nahp.GirlNextDoor.testSetPageName)
    assert(nahp.GirlNextDoor.expectedImageURLs === SGPageParser.parseSetPageToImageURLs(nahpSource))

  }

  test("Dalmasca set page, - hopeful - 3 albums") {
    val dalmasca = TestData.Dalmasca
    val result = testSetHeaderParsing(dalmasca)

    val resultPickerUppers = result.head
    testSetHeader(dalmasca.PickerUppers, resultPickerUppers)
  }
}

private sealed abstract class Set {
  //contains path to where test data for this set can be found
  val testSetPageName: String

  //actual expected values for this set
  val date: String
  val title: String
  val expectedImageURLs: List[String]
}

private sealed abstract class SG {
  val name: String
  val testSetAlbumPageName: String
  val numberOfExpectedSets: Int
}

private object TestData {

  //Dalmasca is a Hopeful
  //has 3 sets and 2 misc albums. These two shouldn't be downloaded
  object Dalmasca extends SG {
    override val name = "Dalmasca"
    override val testSetAlbumPageName = "set-album-page-dalmasca.html"
    override val numberOfExpectedSets = 3

    object PickerUppers extends Set {
      override val testSetPageName = ""

      override val date = "2013.03"
      override val title = "Picker-Uppers"
      override val expectedImageURLs = List()
    }
  }

  //contains both pink and mr sets
  //27 pink
  //3 in MR
  object Sash extends SG {
    override val name = "Sash"
    override val testSetAlbumPageName = "set-album-page-sash.html"
    override val numberOfExpectedSets = 30

    //in the version of the site saved on the hard drive, this is a
    //set in MR
    object TheGrove extends Set {
      override val testSetPageName = ""

      override val date = "2013.04"
      override val title = "The Grove"
      override val expectedImageURLs = List()
    }

    //this is a pink Set
    object Arboraceaous extends Set {
      override val testSetPageName = ""
      override val date = "2013.03"
      override val title = "Arboraceous"
      override val expectedImageURLs = List()
    }
  }

  //Nahp has only pink sets
  object Nahp extends SG {
    override val name = "Nahp"
    override val testSetAlbumPageName = "set-album-page-nahp.html"
    override val numberOfExpectedSets = 12

    object GirlNextDoor extends Set {
      override val testSetPageName = "set-nahp-girl-next-door.html"

      override val date = "2013.01"
      override val title = "Girl Next Door"
      override val expectedImageURLs = List("http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/01.jpg",
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

}

