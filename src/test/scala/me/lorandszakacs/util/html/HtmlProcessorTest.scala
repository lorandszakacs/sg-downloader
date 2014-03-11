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

object HtmlProcessorTest {
  private def readTestData(name: String) = {
    val testDataFolder = TestDataResolver.getTestDataFolderForClass(HtmlProcessorTest.this.getClass(), TestConstants.ProjectName)
    val lines = IO.readLines(testDataFolder + "/" + name)
    lines.mkString("\n")
  }

  def getProcessor(data: String) = {
    val toParse = readTestData(data)
    HtmlProcessor(toParse)
  }

  object ComplexData {
    final val AlbumPageSetOfTheDay = "album-page-sets-of-the-day.html"
    final val AlbumPageMemberReview = "album-page-member-review-sets.html"
    final val PhotoSetOfTheDay = "photo-set-page.html"
    final val PhotoSetMemberReview = "photo-set-member-review-page.html"
  }

  object SimpleData {
    final val Folder = "simplified-filter-test-data/"
    final val FlatLinks = Folder + "flat-links.html"
    final val NestedLinks = Folder + "nested-links.html"
    final val SingleLink = Folder + "single-link.html"
  }

}

class HtmlProcessorTest extends FunSpec with BeforeAndAfter {
  import HtmlProcessorTest._

  describe("HrefLink filter") {
    it("should filter out the one link in the html") {
      val html = getProcessor(SimpleData.SingleLink)
      val links = html filter HrefLink()
      assert(1 === links.length)
      assert(links(0) === "first-link/foo")
    }

    it("should filter out the two nested links in the html") {
      val html = getProcessor(SimpleData.NestedLinks)
      val links = html filter HrefLink()
      assert(2 === links.length)
      assert(links(0) === "first-link/foo")
      assert(links(1) === "second-link/foo")
    }

    it("should filter out two flat links") {
      val html = getProcessor(SimpleData.FlatLinks)
      val links = html filter HrefLink()
      assert(2 === links.length)
      assert(links(0) === "first-link/foo")
      assert(links(1) === "second-link/foo")
    }
  }

  describe("Class filter") {
    it("should filter out the 4 existing `image-section` classes") {
      val html = getProcessor(ComplexData.AlbumPageMemberReview)
      val classes = html filter Class("image-section")
      //FIXME: make more thourough checks
      assert(4 === classes.length)
    }

    it("should filter out the 9 existing `image-section` classes") {
      val html = getProcessor(ComplexData.AlbumPageSetOfTheDay)
      val classes = html filter Class("image-section")
      //FIXME: make more thourough checks
      assert(9 === classes.length)
    }
  }

  describe("Attribute filter") {

  }

  describe("RetainAll combined with HrefLink filter") {
    it("should retain only the first link when composed with a RetainFirst filter, on rhs") {
      val html = getProcessor(SimpleData.NestedLinks)
      val links = html filter HrefLink() && RetainFirst()
      assert(1 === links.length)
      assert(links(0) === "first-link/foo")
    }

    it("should retain only the first link when composed with a RetainFirst filter, on lhs") {
      val html = getProcessor(SimpleData.NestedLinks)
      val links = html filter RetainFirst() && HrefLink()
      assert(1 === links.length)
      assert(links(0) === "first-link/foo")
    }
  }

  //  describe("Generic filtering of the HTML content") {
  //    it("should filter by class") {
  //      val html = readTestData(AlbumPageMemberReview)
  //      val parser = HtmlParser(html)
  //      val classes = parser filter ClassFilter("image-section")
  //      //FIXME: make more thourough checks
  //      assert(4 === classes.length)
  //    }
  //
  //    it("should filter by class and content") {
  //      val html = readTestData(AlbumPageMemberReview)
  //      val parser = HtmlParser(html)
  //      val classes = parser filter (ClassFilter("image-section") && AttributeFilter("href"))
  //      //FIXME: make more thourough checks
  //      assert(4 === classes.length)
  //    }
  //
  //    it("should filter out all the links within all 'image-section' classes") {
  //      val html = readTestData(AlbumPageMemberReview)
  //      val parser = HtmlParser(html)
  //      val classes = parser filter ClassFilter("image-section") && HrefLinkFilter()
  //      println(classes mkString "\n\n")
  //      assert(4 === classes.length)
  //    }
  //  }
  //
  //
  //  }
  //
  //  describe("An HtmlParser grabing the contents of a class") {
  //    val testAlbumPage = PhotoSetOfTheDay
  //    val classForTitle = "title"
  //    val contentForTitle = "Limportance d etre Ernest"
  //
  //    val classForDate = "icon-photography"
  //    val contentForDate = "Jan 24, 2013"
  //
  //    it("should return the contents of the title class") {
  //      val doc = Jsoup.parse(readTestData(testAlbumPage))
  //      val res = doc.getElementsByTag("time")
  //      println(res)
  //      //FIXME
  //
  //    }
  //  }
  //
  //  describe("An HtmlParser filtering with `member-review-sets-page` data") {
  //    val testAlbumPage = AlbumPageMemberReview
  //    val classForAlbum = "image-section"
  //
  //    it("should return 4 elements after filtering by class=image-section") {
  //      val testFileContents = readTestData(testAlbumPage)
  //      val parser = HtmlParser(testFileContents)
  //      val classes = parser.filterByClass(classForAlbum)
  //      assert(4 === classes.length)
  //    }
  //  }
  //
  //  describe("An HtmlParser filtering with `set-of-the-day-page` data") {
  //
  //    val testAlbumPage = PhotoSetOfTheDay
  //    val classForAlbum = "photo-container"
  //
  //    it("should return 45 elements after filtering by class=photo-container") {
  //      val testFileContents = readTestData(testAlbumPage)
  //      val parser = HtmlParser(testFileContents)
  //      val classes = parser.filterByClass(classForAlbum)
  //      assert(45 === classes.length)
  //    }
  //  }

}