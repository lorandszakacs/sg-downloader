package me.lorandszakacs.util.html

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import me.lorandszakacs.util.test.TestDataResolver
import me.lorandszakacs.util.io.IO
import org.scalatest.FunSpec
import org.jsoup.Jsoup

class HtmlParserTest extends FunSpec with BeforeAndAfter {

  describe("And HtmlParser grabbing the first link") {

    val expected = "/girls/dwam/album/1239337/adieu-tristesse/"
    def testLinkGrabbingSuccess(input: String) {
      val result = HtmlParser(input).grabFirstLink()
      result match {
        case None => fail("Should have found a link")
        case Some(result) => assert(expected === result)
      }
    }

    it("should match a standalone link with no spaces") {
      val toMatch = "<a href=\"%s\">".format(expected)
      testLinkGrabbingSuccess(toMatch)
    }

    it("should match a standalone link with spaces after equals") {
      val toMatch = "<a href=  \"%s\">".format(expected)
      testLinkGrabbingSuccess(toMatch)
    }

    it("should match a standalone link with spaces before equals") {
      val toMatch = "<a href  =\"%s\">".format(expected)
      testLinkGrabbingSuccess(toMatch)
    }

    it("should match a standalone link with non-matched junk at the start") {
      val toMatch = "this is just random html junk <a></a>  <a href=\"%s\">".format(expected)
      testLinkGrabbingSuccess(toMatch)
    }

    it("should match only the first link when trailed by another link") {
      val toMatch = "<a href=\"%s\"> <a>html junk</a> <a href=\"not/a/proper/link.jpg\">".format(expected)
      testLinkGrabbingSuccess(toMatch)
    }
    
    it("should match a link on a multi line string") {
      val toMatch = "<a junk> </a>\n<a href=\"%s\"> <a>html junk</a> <a href=\"not/a/proper/link.jpg\">".format(expected)
      testLinkGrabbingSuccess(toMatch)
    }

  }

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
    }
  }

}