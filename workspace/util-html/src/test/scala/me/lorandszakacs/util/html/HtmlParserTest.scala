/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Lorand Szakacs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.lorandszakacs.util.html

import org.scalatest.BeforeAndAfter
import org.scalatest.FunSpec
import me.lorandszakacs.util.io.IO
import me.lorandszakacs.util.test.TestDataResolver
import org.jsoup.Jsoup

object HtmlParserTest {
  def readTestData(name: String) = {
    val testDataFolder = TestDataResolver.getTestDataFolderForClass(this.getClass(), TestConstants.ProjectName)
    val lines = IO.readLines(testDataFolder + "/" + name)
    lines.mkString("\n")
  }

  final val AlbumPageSetOfTheDay = "album-page-sets-of-the-day-dwam.html"
  final val AlbumPageMemberReview = "album-page-member-review-sets-dwam.html"
  final val PhotoSetOfTheDay = "photo-set-page-dwam-limportance-setre-ernest.html"
  final val PhotoSetMemberReview = "photo-set-page-member-review-dwam-adieu-tristesse.html"
}

class HtmlParserTest extends FunSpec with BeforeAndAfter {
  import HtmlParserTest._

  describe("filtering out links with the HTML parser") {
    def getParser(data: String) = {
      val toParse = readTestData(data)
      HtmlParser(toParse)
    }

    it("should filter out the one link in the html") {
      val parser = getParser("simplified-filter-test-data/nested-links.html")
      val links = parser filter LinkFilter()
      assert(1 === links.length)
      assert(links(0) === "first-link/foo")
    }

    it("should filter out the two nested links in the html") {
      val parser = getParser("simplified-filter-test-data/nested-links.html")
      val links = parser filter LinkFilter()
      assert(2 === links.length)
      assert(links(0) === "first-link/foo")
      assert(links(1) === "second-link/foo")

    }

  }

  describe("Generic filtering of the HTML content") {
    it("should filter by class") {
      val html = readTestData(AlbumPageMemberReview)
      val parser = HtmlParser(html)
      val classes = parser filter ClassFilter("image-section")
      //FIXME: make more thourough checks
      assert(4 === classes.length)
    }

    it("should filter by class and content") {
      val html = readTestData(AlbumPageMemberReview)
      val parser = HtmlParser(html)
      val classes = parser filter (ClassFilter("image-section") && AttributeFilter("href"))
      //FIXME: make more thourough checks
      assert(4 === classes.length)
    }

    it("should filter out all the links within all 'image-section' classes") {
      val html = readTestData(AlbumPageMemberReview)
      val parser = HtmlParser(html)
      val classes = parser filter ClassFilter("image-section") && LinkFilter()
      println(classes mkString "\n\n")
      assert(4 === classes.length)
    }
  }

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

    it("should match a real life example") {
      val toMatch = readTestData("simplified-filter-test-data/should-match-complex-thing.html")
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

  describe("An HtmlParser grabing the contents of a class") {
    val testAlbumPage = PhotoSetOfTheDay
    val classForTitle = "title"
    val contentForTitle = "Limportance d etre Ernest"

    val classForDate = "icon-photography"
    val contentForDate = "Jan 24, 2013"

    it("should return the contents of the title class") {
      val doc = Jsoup.parse(readTestData(testAlbumPage))
      val res = doc.getElementsByTag("time")
      println(res)
      //FIXME

    }
  }

  describe("An HtmlParser filtering with `member-review-sets-page` data") {
    val testAlbumPage = AlbumPageMemberReview
    val classForAlbum = "image-section"

    it("should return 4 elements after filtering by class=image-section") {
      val testFileContents = readTestData(testAlbumPage)
      val parser = HtmlParser(testFileContents)
      val classes = parser.filterByClass(classForAlbum)
      assert(4 === classes.length)
    }
  }

  describe("An HtmlParser filtering with `set-of-the-day-page` data") {

    val testAlbumPage = PhotoSetOfTheDay
    val classForAlbum = "photo-container"

    it("should return 45 elements after filtering by class=photo-container") {
      val testFileContents = readTestData(testAlbumPage)
      val parser = HtmlParser(testFileContents)
      val classes = parser.filterByClass(classForAlbum)
      assert(45 === classes.length)
    }
  }

}