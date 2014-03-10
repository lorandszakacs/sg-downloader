package me.lorandszakacs.util.html

import org.scalatest.BeforeAndAfter
import org.scalatest.FunSpec
import me.lorandszakacs.util.io.IO
import me.lorandszakacs.util.test.TestDataResolver
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

    def testLinkGrabbingFailure(input: String) {
      val result = HtmlParser(input).grabFirstLink()
      result match {
        case None => assert(true)
        case Some(_) => fail("should not have found any links")
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

    it("should not match any link in simple example") {
      val toMatch = "<a >"
      testLinkGrabbingFailure(toMatch)
    }

    it("should not match any link in example with a link, but no tags to identify it") {
      val toMatch = "<a \"%s\">".format(expected)
      testLinkGrabbingFailure(toMatch)
    }

  }

  private def readTestData(name: String) = {
    val testDataFolder = TestDataResolver.getTestDataFolderForClass(this.getClass(), TestConstants.ProjectName)
    val lines = IO.readLines(testDataFolder + "/" + name)
    lines.mkString("\n")
  }

  describe("An HtmlParser grabing the contents of a class") {
    val testAlbumPage = "photo-set-page-dwam-limportance-setre-ernest.html"
    val classForTitle = "title"
    val contentForTitle = "Limportance d etre Ernest"

    val classForDate = "icon-photography"
    val contentForDate = "Jan 24, 2013"

    it("should return the contents of the title class") {
      val doc = Jsoup.parse(readTestData(testAlbumPage))
      val res = doc.getElementsByTag("time")
      println("aa")
      
    }
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