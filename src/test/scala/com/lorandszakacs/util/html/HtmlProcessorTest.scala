/**
 * Copyright 2015 Lorand Szakacs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.lorandszakacs.util.html

import org.scalatest.FunSpec

import com.lorandszakacs.util.html.data._

/**
 * @author Lorand Szakacs, lsz@lorandszakacs.com
 * @since 16 Mar 2015
 *
 */
class HtmlProcessorTest extends FunSpec {

  describe("Tag filter") {
    it("should return the only tag") {
      val html = Html(SimplifiedData.FilterTag.SingleTag)
      val tag = html filter Tag("a")
      tag match {
        case None => fail("should have found some tag")
        case Some(tag) => assert(1 === tag.length)
      }
    }

    it("should return both flat tags") {
      val html = Html(SimplifiedData.FilterTag.FlatTags)
      val tag = html filter Tag("a")
      tag match {
        case None => fail("should have found some tag")
        case Some(tag) => assert(2 === tag.length)
      }
    }

    it("should return all three nested tags") {
      val html = Html(SimplifiedData.FilterTag.NestedTags)
      val tag = html filter Tag("a")
      tag match {
        case None => fail("should have found some tag")
        case Some(tag) => assert(3 === tag.length)
      }
    }

    it("should return the elements with the specified tag from within all the top level elements") {
      val html = Html(SimplifiedData.FilterTag.NestedTagsWithinFlatTags)
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
      val html = Html(SimplifiedData.FilterTag.NestedTags)
      val tag = html filter Tag("no-such-tag")
      tag match {
        case None => info("properly returned `None`")
        case Some(_) => fail("should not have found anything")
      }
    }

    it("should return all 45 elements with tag `li`") {
      val html = Html(RealLifeData.PhotoSetOfTheDay)
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
      val html = Html(SimplifiedData.FilterClass.SingleClass)
      val clazz = html filter Class("meta-data")
      clazz match {
        case None => fail("should have found some class")
        case Some(clazz) => assert(1 === clazz.length)
      }
    }

    it("should return both flat classes") {
      val html = Html(SimplifiedData.FilterClass.FlatClasses)
      val clazz = html filter Class("meta-data")
      clazz match {
        case None => fail("should have found some class")
        case Some(clazz) => assert(2 === clazz.length)
      }

    }

    it("should return both nested classes") {
      val html = Html(SimplifiedData.FilterClass.NestedClasses)
      val clazz = html filter Class("meta-data")
      clazz match {
        case None => fail("should have found some class")
        case Some(clazz) => assert(2 === clazz.length)
      }
    }

    //FIXME: this fails because of a bug in Jsoup where classes with spaces are parsed as two seperate classes.
    ignore("should return elements with classes containing spaces") {
      val html = Html(SimplifiedData.FilterClass.ClassWithSpaceInTheName)
      val clazz = html filter Class("button login")
      clazz match {
        case None => fail("should have found some class")
        case Some(clazz) => assert(1 === clazz.length)
      }

    }

    it("should return `None` when looking for a class that doesn't exists") {
      val html = Html(SimplifiedData.FilterClass.NestedClasses)
      val clazz = html filter Class("no-such-class")
      clazz match {
        case None => info("properly returned `None`")
        case Some(_) => fail("should not have found anything")
      }
    }

    it("should filter out the 4 existing `image-section` classes") {
      val html = Html(RealLifeData.AlbumPageMemberReview)
      val classes = html filter Class("image-section")
      classes match {
        case None => fail("should have found some class")
        case Some(classes) => assert(4 === classes.length)
      }
    }

    it("should filter out the 9 existing `image-section` classes") {
      val html = Html(RealLifeData.AlbumPageSetOfTheDay)
      val classes = html filter Class("image-section")
      classes match {
        case None => fail("should have found some class")
        case Some(classes) => assert(9 === classes.length)
      }
    }

    it("should filter out the 45 existing `photo-container` classes") {
      val html = Html(RealLifeData.PhotoSetOfTheDay)
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
      val html = Html(RealLifeData.AlbumPageMemberReview)
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
      val html = Html(SimplifiedData.FilterTag.NestedTags)
      val attribute = html filter Attribute("no-such-attr")
      attribute match {
        case None => info("properly returned `None`")
        case Some(_) => fail("should not have found anything")
      }
    }
  }

  describe("grabbing the Values of elements filtered by Attributes") {
    it("should return `data-index`attribute contents") {
      val html = Html(RealLifeData.PhotoSetOfTheDay)
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
      val html = Html(SimplifiedData.FilterLink.SingleLink)
      val links = html filter HrefLink()
      links match {
        case None => fail("should have found some hrefs")
        case Some(links) =>
          assert(1 === links.length)
          assert(links(0) === "first-link/foo")
      }
    }

    it("should filter out the two nested links in the html") {
      val html = Html(SimplifiedData.FilterLink.NestedLinks)
      val links = html filter HrefLink()
      links match {
        case None => fail("should have found some hrefs")
        case Some(links) =>
          assert(2 === links.length)
          assert(links(0) === "first-link/foo")
          assert(links(1) === "second-link/foo")
      }
    }

    it("should filter out two flat links") {
      val html = Html(SimplifiedData.FilterLink.FlatLinks)
      val links = html filter HrefLink()
      links match {
        case None => fail("should have found some hrefs")
        case Some(links) =>
          assert(2 === links.length)
          assert(links(0) === "first-link/foo")
          assert(links(1) === "second-link/foo")
      }
    }
  }

  describe("RetainFirst of HrefLink filter") {
    it("should retain only the first link when composed with a RetainFirst filter, on rhs") {
      val html = Html(SimplifiedData.FilterLink.NestedLinks)
      val links = html filter RetainFirst(HrefLink())
      links match {
        case None => fail("should have found some hrefs")
        case Some(links) =>
          assert(1 === links.length)
          assert(links(0) === "first-link/foo")
      }
    }

    it("should retain only the first link when composed with a RetainFirst filter, on lhs") {
      val html = Html(SimplifiedData.FilterLink.NestedLinks)
      val links = html filter RetainFirst(HrefLink())
      links match {
        case None => fail("should have found some hrefs")
        case Some(links) =>
          assert(1 === links.length)
          assert(links(0) === "first-link/foo")
      }
    }
  }

  describe("Content filter") {
    it("should return only the date from the `icon-photography` class") {
      val html = Html(SimplifiedData.FilterContent.ContentFromClass)
      val content = html filter Content(Class("icon-photography"))
      content match {
        case None => fail("should have found something")
        case Some(date) =>
          assert(1 === date.length)
          assert("Nov 09, 2013" === date(0).trim)
      }
    }

    it("should return only the contents of the `div` tag") {
      val html = Html(SimplifiedData.FilterContent.ContentFromTag)
      val content = html filter Content(Tag("div"))
      content match {
        case None => fail("should have found something")
        case Some(div) =>
          assert(1 === div.length)
          assert("<a>whatever</a>" === div(0).trim)
      }
    }

    it("should return only the contents of the `id` attribute") {
      val html = Html(SimplifiedData.FilterContent.ContentFromAttribute)
      val content = html filter Content(Attribute("id"))
      content match {
        case None => fail("should have found something")
        case Some(loadMore) =>
          assert(1 === loadMore.length)
          assert("Load more" === loadMore(0).trim)
      }
    }

    it("should return the contents of a Composite Filter") {
      val html = Html(SimplifiedData.FilterContent.ContentFromComposite)
      val content = html filter Content(Class("meta-data") && Class("photographer"))
      content match {
        case None => fail("should have found something")
        case Some(by) =>
          assert(1 === by.length)
          assert(by(0).trim.startsWith("by"))
      }
    }
  }

  describe("Combining filters") {
    it("should return all the links contained within the `photo-container` classes contained within the first `image-section` class") {
      val html = Html(ComplexData.Combination.TwoTopLevelImageSections)
      val links = html filter RetainFirst(Class("image-section")) && Class("photo-container") && HrefLink()
      links match {
        case None => fail("should have found some hrefs")
        case Some(links) =>
          assert(45 === links.length)
          assert("link0" === links(0))
          assert("link44" === links(44))
      }
    }

    it("should return all the links contained within the `photo-container` classes contained within both `image-section` class") {
      val html = Html(ComplexData.Combination.TwoTopLevelImageSections)
      val links = html filter Class("image-section") && Class("photo-container") && HrefLink()
      links match {
        case None => fail("should have found some hrefs")
        case Some(links) =>
          assert(46 === links.length)
          assert("link0" === links(0))
          assert("link44" === links(44))
          assert("BOGUS LINK!!" === links(45))
      }
    }

    it("should return all the links contained within the `photo-container` classes contained within both `image-section` class" +
      "even though the middle `image-section` class contains no photocontainers") {
      val html = Html(ComplexData.Combination.ThreeTopLevelImageSectionsMiddleOneEmpty)
      val links = html filter Class("image-section") && Class("photo-container") && HrefLink()
      links match {
        case None => fail("should have found some hrefs")
        case Some(links) =>
          assert(3 === links.length)
          assert("link0" === links(0))
          assert("link44" === links(1))
          assert("BOGUS LINK!!" === links(2))
      }
    }

    it("should return `None` if the first filter in the combination returns `None`") {
      val html = Html(ComplexData.Combination.TwoTopLevelImageSections)
      val links = html filter Class("non-existent-class") && Class("photo-container") && HrefLink()
      links match {
        case None => info("returned `None`, as expected")
        case Some(_) => fail("should not have found any links")
      }
    }

    it("should return `None` if the filter in middle the combination returns `None`") {
      val html = Html(ComplexData.Combination.TwoTopLevelImageSections)
      val links = html filter Class("image-section") && Class("non-existent-class") && HrefLink()
      links match {
        case None => info("returned `None`, as expected")
        case Some(_) => fail("should not have found anything")
      }
    }

    it("should return `None` if the last filter in the combination returns `None`") {
      val html = Html(ComplexData.Combination.TwoTopLevelImageSections)
      val links = html filter Class("image-section") && Class("photo-container") && Attribute("non-existent-attribute")
      links match {
        case None => info("returned `None`, as expected")
        case Some(_) => fail("should not have found anything")
      }
    }

  }
}