/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Lorand Szakacs
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
package com.lorandszakacs.util.html

import java.io.File
import org.scalatest.FunSpec
import com.lorandszakacs.util.html.data.ComplexData
import com.lorandszakacs.util.html.data.SimplifiedData
import com.lorandszakacs.util.html.data.RealLifeData

object HtmlProcessorTest {
  def testDataFolder = TestDataResolver.getTestDataFolderForClass(HtmlProcessorTest.this.getClass(), TestConstants.ProjectName)

  def getProcessor(data: String) = HtmlProcessor(new File(testDataFolder + "/" + data))

  object Data {

    object Complex {
      private final val TopLevelFolder = "complex-data/"

      //      object Filter {
      //        private final val Folder = Complex.TopLevelFolder + "filter/"
      //        final val FilterByClass = Folder + "filter-by-button-login-class.html"
      //      }

      //      object Unsorted {
      //
      //      }

      //      final val AlbumPageSetOfTheDay = "album-page-sets-of-the-day.html"
      //      final val AlbumPageMemberReview = "album-page-member-review-sets.html"
      //      final val PhotoSetOfTheDay = "photo-set-of-the-day-page.html"
      //      final val PhotoSetMemberReview = "photo-set-member-review-page.html"
    }

    object Simple {
      private final val TopLeveLFolder = "simplified-data/"

      //      object Link {
      //        private final val Folder = Simple.TopLeveLFolder + "filter-link/"
      //        final val Flat = Folder + "flat-links.html"
      //        final val Nested = Folder + "nested-links.html"
      //        final val Single = Folder + "single-link.html"
      //      }

      //      object Tag {
      //        private final val Folder = Simple.TopLeveLFolder + "filter-tag/"
      //        final val Flat = Folder + "flat-tags.html"
      //        final val Nested = Folder + "nested-tags.html"
      //        final val Single = Folder + "single-tag.html"
      //        final val NestedWithinFlat = Folder + "nested-tags-within-flat-tags.html"
      //      }

      //      object Class {
      //        private final val Folder = Simple.TopLeveLFolder + "filter-class/"
      //        final val Flat = Folder + "flat-classes.html"
      //        final val Nested = Folder + "nested-classes.html"
      //        final val Single = Folder + "single-class.html"
      //        final val Space = Folder + "class-with-space-in-name.html"
      //      }

      //      object Content {
      //        private final val Folder = Simple.TopLeveLFolder + "filter-content/"
      //        final val FromClass = Folder + "content-from-class.html"
      //        final val FromTag = Folder + "content-from-tag.html"
      //        final val FromAttribute = Folder + "content-from-attribute.html"
      //        final val FromComposite = Folder + "content-from-composite.html"
      //      }
    }
  }
}

class HtmlProcessorTest extends FunSpec {
  import HtmlProcessorTest._

  describe("Tag filter") {
    it("should return the only tag") {
      val html = HtmlProcessor(SimplifiedData.FilterTag.SingleTag)
      val tag = html filter Tag("a")
      tag match {
        case None => fail("should have found some tag")
        case Some(tag) => assert(1 === tag.length)
      }
    }

    it("should return both flat tags") {
      val html = HtmlProcessor(SimplifiedData.FilterTag.FlatTags)
      val tag = html filter Tag("a")
      tag match {
        case None => fail("should have found some tag")
        case Some(tag) => assert(2 === tag.length)
      }
    }

    it("should return all three nested tags") {
      val html = HtmlProcessor(SimplifiedData.FilterTag.NestedTags)
      val tag = html filter Tag("a")
      tag match {
        case None => fail("should have found some tag")
        case Some(tag) => assert(3 === tag.length)
      }
    }

    it("should return the elements with the specified tag from within all the top level elements") {
      val html = HtmlProcessor(SimplifiedData.FilterTag.NestedTagsWithinFlatTags)
      val tag = html filter Tag("a")
      tag match {
        case None => fail("should have found some tag")
        case Some(tag) => {
          assert(6 === tag.length)
          assert(tag(0).startsWith("<a href=\"first-link"))
          assert(tag(5).trim.startsWith("<a href=\"second/third-link"))
        }
      }
    }

    it("should return `None` when looking for a tag that doesn't exists") {
      val html = HtmlProcessor(SimplifiedData.FilterTag.NestedTags)
      val tag = html filter Tag("no-such-tag")
      tag match {
        case None => info("properly returned `None`")
        case Some(_) => fail("should not have found anything")
      }
    }

    it("should return all 45 elements with tag `li`") {
      val html = HtmlProcessor(RealLifeData.PhotoSetOfTheDay)
      val tags = html filter Tag("li")
      tags match {
        case None => fail("should have found some tag")
        case Some(tags) => {
          assert(45 === tags.length)
          assert(tags.forall(tag => tag.startsWith("<li class=\"photo-container\"")))
        }
      }
    }
  }

  describe("Class filter") {
    it("should return the only class") {
      val html = HtmlProcessor(SimplifiedData.FilterClass.SingleClass)
      val clazz = html filter Class("meta-data")
      clazz match {
        case None => fail("should have found some class")
        case Some(clazz) => assert(1 === clazz.length)
      }
    }

    it("should return both flat classes") {
      val html = HtmlProcessor(SimplifiedData.FilterClass.FlatClasses)
      val clazz = html filter Class("meta-data")
      clazz match {
        case None => fail("should have found some class")
        case Some(clazz) => assert(2 === clazz.length)
      }

    }

    it("should return both nested classes") {
      val html = HtmlProcessor(SimplifiedData.FilterClass.NestedClasses)
      val clazz = html filter Class("meta-data")
      clazz match {
        case None => fail("should have found some class")
        case Some(clazz) => assert(2 === clazz.length)
      }
    }

    //FIXME: this fails because of a bug in Jsoup where classes with spaces are parsed as two seperate classes.
    ignore("should return elements with classes containing spaces") {
      val html = HtmlProcessor(SimplifiedData.FilterClass.ClassWithSpaceInTheName)
      val clazz = html filter Class("button login")
      clazz match {
        case None => fail("should have found some class")
        case Some(clazz) => assert(1 === clazz.length)
      }

    }

    it("should return `None` when looking for a class that doesn't exists") {
      val html = HtmlProcessor(SimplifiedData.FilterClass.NestedClasses)
      val clazz = html filter Class("no-such-class")
      clazz match {
        case None => info("properly returned `None`")
        case Some(_) => fail("should not have found anything")
      }
    }

    it("should filter out the 4 existing `image-section` classes") {
      val html = HtmlProcessor(RealLifeData.AlbumPageMemberReview)
      val classes = html filter Class("image-section")
      classes match {
        case None => fail("should have found some class")
        case Some(classes) => assert(4 === classes.length)
      }
    }

    it("should filter out the 9 existing `image-section` classes") {
      val html = HtmlProcessor(RealLifeData.AlbumPageSetOfTheDay)
      val classes = html filter Class("image-section")
      classes match {
        case None => fail("should have found some class")
        case Some(classes) => assert(9 === classes.length)
      }
    }

    it("should filter out the 45 existing `photo-container` classes") {
      val html = HtmlProcessor(RealLifeData.PhotoSetOfTheDay)
      val classes = html filter Class("photo-container")
      classes match {
        case None => fail("should have found some class")
        case Some(classes) => {
          assert(45 === classes.length)
          assert(classes.forall(clazz => clazz.startsWith("<li class=\"photo-container\"")))
        }
      }
    }
  }

  describe("Attribute filter") {
    it("should filter out the 4 existing elements with a `href` attribute") {
      val html = HtmlProcessor(RealLifeData.AlbumPageMemberReview)
      val hrefs = html filter Attribute("href")
      hrefs match {
        case None => fail("should have found some attribute")
        case Some(hrefs) => {
          assert(4 === hrefs.length)
          assert(hrefs(0).startsWith("<a href=\"href1"))
          assert(hrefs(3).startsWith("<a href=\"href4"))
        }
      }
    }

    it("should return `None` when looking for an attribute that doesn't exists") {
      val html = HtmlProcessor(SimplifiedData.FilterTag.NestedTags)
      val attribute = html filter Attribute("no-such-attr")
      attribute match {
        case None => info("properly returned `None`")
        case Some(_) => fail("should not have found anything")
      }
    }
  }

  describe("grabbing the Values of elements filtered by Attributes") {
    it("should return `data-index`attribute contents") {
      val html = HtmlProcessor(RealLifeData.PhotoSetOfTheDay)
      val dataIndex = html filter Value(Attribute("data-index"))
      dataIndex match {
        case None => fail("should have found some dataIndex")
        case Some(dataIndex) => {
          assert(dataIndex.length === 45)
          assert(dataIndex(0) === "0")
          assert(dataIndex(44) === "44")
        }
      }
    }
  }

  describe("HrefLink filter") {
    it("should filter out the one link in the html") {
      val html = HtmlProcessor(SimplifiedData.FilterLink.SingleLink)
      val links = html filter HrefLink()
      links match {
        case None => fail("should have found some hrefs")
        case Some(links) => {
          assert(1 === links.length)
          assert(links(0) === "first-link/foo")
        }
      }
    }

    it("should filter out the two nested links in the html") {
      val html = HtmlProcessor(SimplifiedData.FilterLink.NestedLinks)
      val links = html filter HrefLink()
      links match {
        case None => fail("should have found some hrefs")
        case Some(links) => {
          assert(2 === links.length)
          assert(links(0) === "first-link/foo")
          assert(links(1) === "second-link/foo")
        }
      }
    }

    it("should filter out two flat links") {
      val html = HtmlProcessor(SimplifiedData.FilterLink.FlatLinks)
      val links = html filter HrefLink()
      links match {
        case None => fail("should have found some hrefs")
        case Some(links) => {
          assert(2 === links.length)
          assert(links(0) === "first-link/foo")
          assert(links(1) === "second-link/foo")
        }
      }
    }
  }

  describe("RetainFirst of HrefLink filter") {
    it("should retain only the first link when composed with a RetainFirst filter, on rhs") {
      val html = HtmlProcessor(SimplifiedData.FilterLink.NestedLinks)
      val links = html filter RetainFirst(HrefLink())
      links match {
        case None => fail("should have found some hrefs")
        case Some(links) => {
          assert(1 === links.length)
          assert(links(0) === "first-link/foo")
        }
      }
    }

    it("should retain only the first link when composed with a RetainFirst filter, on lhs") {
      val html = HtmlProcessor(SimplifiedData.FilterLink.NestedLinks)
      val links = html filter RetainFirst(HrefLink())
      links match {
        case None => fail("should have found some hrefs")
        case Some(links) => {
          assert(1 === links.length)
          assert(links(0) === "first-link/foo")
        }
      }
    }
  }

  describe("Content filter") {
    it("should return only the date from the `icon-photography` class") {
      val html = HtmlProcessor(SimplifiedData.FilterContent.ContentFromClass)
      val content = html filter Content(Class("icon-photography"))
      content match {
        case None => fail("should have found something")
        case Some(date) => {
          assert(1 === date.length)
          assert("Nov 09, 2013" === date(0).trim)
        }
      }
    }

    it("should return only the contents of the `div` tag") {
      val html = HtmlProcessor(SimplifiedData.FilterContent.ContentFromTag)
      val content = html filter Content(Tag("div"))
      content match {
        case None => fail("should have found something")
        case Some(div) => {
          assert(1 === div.length)
          assert("<a>whatever</a>" === div(0).trim)
        }
      }
    }

    it("should return only the contents of the `id` attribute") {
      val html = HtmlProcessor(SimplifiedData.FilterContent.ContentFromAttribute)
      val content = html filter Content(Attribute("id"))
      content match {
        case None => fail("should have found something")
        case Some(loadMore) => {
          assert(1 === loadMore.length)
          assert("Load more" === loadMore(0).trim)
        }
      }
    }

    it("should return the contents of a Composite Filter") {
      val html = HtmlProcessor(SimplifiedData.FilterContent.ContentFromComposite)
      val content = html filter Content(Class("meta-data") && Class("photographer"))
      content match {
        case None => fail("should have found something")
        case Some(by) => {
          assert(1 === by.length)
          assert(by(0).trim.startsWith("by"))
        }
      }
    }
  }

  describe("Combining filters") {
    it("should return all the links contained within the `photo-container` classes contained within the first `image-section` class") {
      val html = HtmlProcessor(ComplexData.Combination.TwoTopLevelImageSections)
      val links = html filter RetainFirst(Class("image-section")) && Class("photo-container") && HrefLink()
      links match {
        case None => fail("should have found some hrefs")
        case Some(links) => {
          assert(45 === links.length)
          assert("link0" === links(0))
          assert("link44" === links(44))
        }
      }
    }

    it("should return all the links contained within the `photo-container` classes contained within both `image-section` class") {
      val html = HtmlProcessor(ComplexData.Combination.TwoTopLevelImageSections)
      val links = html filter Class("image-section") && Class("photo-container") && HrefLink()
      links match {
        case None => fail("should have found some hrefs")
        case Some(links) => {
          assert(46 === links.length)
          assert("link0" === links(0))
          assert("link44" === links(44))
          assert("BOGUS LINK!!" === links(45))
        }
      }
    }

    it("should return all the links contained within the `photo-container` classes contained within both `image-section` class" +
      "even though the middle `image-section` class contains no photocontainers") {
      val html = HtmlProcessor(ComplexData.Combination.ThreeTopLevelImageSectionsMiddleOneEmpty)
      val links = html filter Class("image-section") && Class("photo-container") && HrefLink()
      links match {
        case None => fail("should have found some hrefs")
        case Some(links) => {
          assert(3 === links.length)
          assert("link0" === links(0))
          assert("link44" === links(1))
          assert("BOGUS LINK!!" === links(2))
        }
      }
    }

    it("should return `None` if the first filter in the combination returns `None`") {
      val html = HtmlProcessor(ComplexData.Combination.TwoTopLevelImageSections)
      val links = html filter Class("non-existent-class") && Class("photo-container") && HrefLink()
      links match {
        case None => info("returned `None`, as expected")
        case Some(_) => fail("should not have found any links")
      }
    }

    it("should return `None` if the filter in middle the combination returns `None`") {
      val html = HtmlProcessor(ComplexData.Combination.TwoTopLevelImageSections)
      val links = html filter Class("image-section") && Class("non-existent-class") && HrefLink()
      links match {
        case None => info("returned `None`, as expected")
        case Some(_) => fail("should not have found anything")
      }
    }

    it("should return `None` if the last filter in the combination returns `None`") {
      val html = HtmlProcessor(ComplexData.Combination.TwoTopLevelImageSections)
      val links = html filter Class("image-section") && Class("photo-container") && Attribute("non-existent-attribute")
      links match {
        case None => info("returned `None`, as expected")
        case Some(_) => fail("should not have found anything")
      }
    }

  }
}