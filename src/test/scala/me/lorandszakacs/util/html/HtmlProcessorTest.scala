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

  object Data {

    object Complex {
      private final val TopLevelFolder = "complex-data/"

      object Combination {
        private final val Folder = Complex.TopLevelFolder + "combination/"
        final val TwoTopLevelImageSections = Folder + "two-top-level-image-sections.html"
      }

      object Unsorted {

      }

      final val AlbumPageSetOfTheDay = "album-page-sets-of-the-day.html"
      final val AlbumPageMemberReview = "album-page-member-review-sets.html"
      final val PhotoSetOfTheDay = "photo-set-of-the-day-page.html"
      final val PhotoSetMemberReview = "photo-set-member-review-page.html"
    }

    object Simple {
      private final val TopLeveLFolder = "simplified-data/"

      object Link {
        private final val Folder = Simple.TopLeveLFolder + "filter-link/"
        final val Flat = Folder + "flat-links.html"
        final val Nested = Folder + "nested-links.html"
        final val Single = Folder + "single-link.html"
      }

      object Tag {
        private final val Folder = Simple.TopLeveLFolder + "filter-tag/"
        final val Flat = Folder + "flat-tags.html"
        final val Nested = Folder + "nested-tags.html"
        final val Single = Folder + "single-tag.html"
      }

      object Class {
        private final val Folder = Simple.TopLeveLFolder + "filter-class/"
        final val Flat = Folder + "flat-classes.html"
        final val Nested = Folder + "nested-classes.html"
        final val Single = Folder + "single-class.html"
      }

    }
  }

}

class HtmlProcessorTest extends FunSpec with BeforeAndAfter {
  import HtmlProcessorTest._

  describe("Tag filter") {
    it("should return the only tag") {
      val html = getProcessor(Data.Simple.Tag.Single)
      val tag = html filter Tag("a")
      assert(1 === tag.length)
    }

    it("should return both flat tags") {
      val html = getProcessor(Data.Simple.Tag.Flat)
      val tag = html filter Tag("a")
      assert(2 === tag.length)
    }

    it("should return all three nested tags") {
      val html = getProcessor(Data.Simple.Tag.Nested)
      val tag = html filter Tag("a")
      assert(3 === tag.length)
    }

    it("should return all 45 elements with tag `li`") {
      val html = getProcessor(Data.Complex.PhotoSetOfTheDay)
      val tags = html filter Tag("li")
      //FIXME: make more thorough checks
      assert(45 === tags.length)
    }

    //FIXME: add tests for behavior of two top level tags, each with more nested tags
  }

  describe("Class filter") {
    it("should return the only class") {
      val html = getProcessor(Data.Simple.Class.Single)
      val clazz = html filter Class("meta-data")
      assert(1 === clazz.length)
    }

    it("should return both flat classes") {
      val html = getProcessor(Data.Simple.Class.Flat)
      val clazz = html filter Class("meta-data")
      assert(2 === clazz.length)
    }

    it("should return both nested tags") {
      val html = getProcessor(Data.Simple.Class.Nested)
      val clazz = html filter Class("meta-data")
      assert(2 === clazz.length)
    }

    it("should filter out the 4 existing `image-section` classes") {
      val html = getProcessor(Data.Complex.AlbumPageMemberReview)
      val classes = html filter Class("image-section")
      //FIXME: make more thorough checks
      assert(4 === classes.length)
    }

    it("should filter out the 9 existing `image-section` classes") {
      val html = getProcessor(Data.Complex.AlbumPageSetOfTheDay)
      val classes = html filter Class("image-section")
      //FIXME: make more thorough checks
      assert(9 === classes.length)
    }

    it("should filter out the 45 existing `photo-container` classes") {
      val html = getProcessor(Data.Complex.PhotoSetOfTheDay)
      val classes = html filter Class("photo-container")
      //FIXME: make more thorough checks
      assert(45 === classes.length)
    }
  }

  describe("Attribute filter") {
    it("should filter out the 4 existing elements with a `href` attribute") {
      val html = getProcessor(Data.Complex.AlbumPageMemberReview)
      val hrefs = html filter Attribute("href")
      //FIXME: make more thorough checks
      assert(4 === hrefs.length)
    }
  }

  describe("grabbing Content of elements filtered by Attributes") {
    it("should return `data-index`attribute contents") {
      val html = getProcessor(Data.Complex.PhotoSetOfTheDay)
      val dataIndex = html filter Content(Attribute("data-index"))
      assert(dataIndex.length === 45)
      assert(dataIndex(0) === "0")
      assert(dataIndex(44) === "44")
    }
  }

  describe("HrefLink filter") {
    it("should filter out the one link in the html") {
      val html = getProcessor(Data.Simple.Link.Single)
      val links = html filter HrefLink()
      assert(1 === links.length)
      assert(links(0) === "first-link/foo")
    }

    it("should filter out the two nested links in the html") {
      val html = getProcessor(Data.Simple.Link.Nested)
      val links = html filter HrefLink()
      assert(2 === links.length)
      assert(links(0) === "first-link/foo")
      assert(links(1) === "second-link/foo")
    }

    it("should filter out two flat links") {
      val html = getProcessor(Data.Simple.Link.Flat)
      val links = html filter HrefLink()
      assert(2 === links.length)
      assert(links(0) === "first-link/foo")
      assert(links(1) === "second-link/foo")
    }
  }

  describe("RetainFirst of HrefLink filter") {
    it("should retain only the first link when composed with a RetainFirst filter, on rhs") {
      val html = getProcessor(Data.Simple.Link.Nested)
      val links = html filter RetainFirst(HrefLink())
      assert(1 === links.length)
      assert(links(0) === "first-link/foo")
    }

    it("should retain only the first link when composed with a RetainFirst filter, on lhs") {
      val html = getProcessor(Data.Simple.Link.Nested)
      val links = html filter RetainFirst(HrefLink())
      assert(1 === links.length)
      assert(links(0) === "first-link/foo")
    }
  }

  describe("Combining filters") {
    it("should return all the links contained within the `photo-container` classes contained within the first `image-section` class") {
      val html = getProcessor(Data.Complex.Combination.TwoTopLevelImageSections)
      val links = html filter RetainFirst(Class("image-section")) && Class("photo-container") && HrefLink()
      assert(45 === links.length)
      assert("link0" === links(0))
      assert("link44" === links(44))
    }

    it("should return all the links contained within the `photo-container` classes contained within both `image-section` class") {
      val html = getProcessor(Data.Complex.Combination.TwoTopLevelImageSections)
      val links = html filter Class("image-section") && Class("photo-container") && HrefLink()
      assert(46 === links.length)
      assert("link0" === links(0))
      assert("link44" === links(44))
      assert("BOGUS LINK!!" === links(45))
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