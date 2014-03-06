package me.lorandszakacs.util.html

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import me.lorandszakacs.util.test.TestDataResolver
import me.lorandszakacs.util.io.IO
import org.scalatest.FunSpec

class HtmlParserTest extends FunSpec with BeforeAndAfter {

  private var _testFileContents: String = ""

  val testAlbumPage = "album-page-member-review-sets-dwam.html"
  val classForAlbum = "image-section"

  describe("An HtmlParser") {
    before {

      val testDataFolder = TestDataResolver.getTestDataFolderForClass(this.getClass())
      val lines = IO.readLines(TestDataResolver.getTestDataFolderForClass(this.getClass()) + "/" + testAlbumPage)
      val linesInString = lines.mkString("\n")
      _testFileContents = linesInString
    }

    after {
      _testFileContents = ""
    }

    it("should return 4 elements after filtering by class") {
      val parser = HtmlParser(_testFileContents)
      val classes = parser.filterByClass(classForAlbum)
      assert(4 === classes.length)
    }
  }

}