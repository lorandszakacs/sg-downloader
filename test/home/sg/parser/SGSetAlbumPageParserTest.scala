package home.sg.parser
import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.io.File
import scala.io.Source
import home.sg.util.TestDataResolver

@RunWith(classOf[JUnitRunner])
class SGSetAlbumPageParserTest extends FunSuite {

  private def getTestSourceFile(fileName: String) = {
    val filePath = TestDataResolver.getTestDataFolderForClass(classOf[SGSetAlbumPageParserTest]) + fileName
    scala.io.Source.fromFile(new File(filePath))
  }

  test("Nahp Set-Album Page, 12 total sets") {
    val nahpSource = getTestSourceFile(SGParserTestData.nahpSetAlbumPage)
    val result = SGSetAlbumPageParser.parseSetAlbumPage(nahpSource);
    assert(result.length === 12)
  }
  
  test("Sash Set-Album Page, 30 total sets") {
    val sashSource = getTestSourceFile(SGParserTestData.sashSetAlbumPage)
    val result = SGSetAlbumPageParser.parseSetAlbumPage(sashSource)
    assert(result.length == 30)
  }
}

