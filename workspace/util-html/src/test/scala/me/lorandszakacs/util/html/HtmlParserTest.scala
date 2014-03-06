package me.lorandszakacs.util.html

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import me.lorandszakacs.util.test.TestDataResolver
import me.lorandszakacs.util.io.IO
import org.scalatest.FunSpec

class HtmlParserTest extends FunSpec with BeforeAndAfter {

  private def readTestData(name: String) = {
    val testDataFolder = TestDataResolver.getTestDataFolderForClass(this.getClass())
    val lines = IO.readLines(TestDataResolver.getTestDataFolderForClass(this.getClass()) + "/" + name)
    lines.mkString("\n")
  }

  describe("An HtmlParser filtering with `member-review-sets-page` data") {

    val testAlbumPage = "album-page-member-review-sets-dwam.html"
    val classForAlbum = "image-section"

    it("should return 4 elements after filtering by class=image-section") {
      val testFileContents = readTestData(testAlbumPage)
      val parser = HtmlParser(testFileContents)
      val classes = parser.filterByClass(classForAlbum)
      assert(4 === classes.length)
    }
  }

  describe("An HtmlParser filtering with `set-of-the-day-page` data") {

    val testAlbumPage = "photo-set-page-dwam-limportance-setre-ernest.html"
    val classForAlbum = "photo-container"

    it("should return 45 elements after filtering by class=photo-container") {
      val testFileContents = readTestData(testAlbumPage)
      val parser = HtmlParser(testFileContents)
      val classes = parser.filterByClass(classForAlbum)
      assert(45 === classes.length)
      println(classes.mkString("\n\n\n"))
    }
  }

}