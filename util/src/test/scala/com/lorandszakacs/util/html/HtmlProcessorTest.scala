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

import org.scalatest.{FunSpec, Matchers}

import com.lorandszakacs.util.html.data._

/**
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 16 Mar 2015
  *
  */
class HtmlProcessorTest extends FunSpec with Matchers {

  describe("Tag filter") {
    it("should return the only tag") {
      val html = Html(SimplifiedData.FilterTag.SingleTag)
      val tag  = html.filter(Tag("a"))
      (tag should have).length(1)
    }

    it("should return both flat tags") {
      val html = Html(SimplifiedData.FilterTag.FlatTags)
      val tag  = html.filter(Tag("a"))
      (tag should have).length(2)
    }

    it("should return all three nested tags") {
      val html = Html(SimplifiedData.FilterTag.NestedTags)
      val tag  = html.filter(Tag("a"))
      (tag should have).length(3)
    }

    it("should return the elements with the specified tag from within all the top level elements") {
      val html = Html(SimplifiedData.FilterTag.NestedTagsWithinFlatTags)
      val tag  = html.filter(Tag("a"))
      (tag should have).length(6)
      tag(0) should startWith("""<a href="first-link""")
      tag(5) should startWith("""<a href="second/third-link""")
    }

    it("should return `Nil` when looking for a tag that doesn't exists") {
      val html = Html(SimplifiedData.FilterTag.NestedTags)
      val tag  = html.filter(Tag("no-such-tag"))
      tag shouldBe empty
    }

    it("should return all 45 elements with tag `li`") {
      val html = Html(RealLifeData.PhotoSetOfTheDay)
      val tags = html.filter(Tag("li"))
      (tags should have).length(45)
      tags.zipWithIndex.foreach { pair =>
        val (tag, index) = pair
        withClue(s"@ index $index ...") {
          tag should startWith("""<li class="photo-container"""")
        }
      }
    }
  }

  //==========================================================================
  //==========================================================================

  describe("Class filter") {
    it("should return the only class") {
      val html  = Html(SimplifiedData.FilterClass.SingleClass)
      val clazz = html.filter(Class("meta-data"))
      (clazz should have).length(1)
    }

    it("should return both flat classes") {
      val html  = Html(SimplifiedData.FilterClass.FlatClasses)
      val clazz = html.filter(Class("meta-data"))
      (clazz should have).length(2)
    }

    it("should return both nested classes") {
      val html  = Html(SimplifiedData.FilterClass.NestedClasses)
      val clazz = html.filter(Class("meta-data"))
      (clazz should have).length(2)
    }

    //FIXME: this fails because of a bug in Jsoup where classes with spaces are parsed as two seperate classes.
    ignore("should return elements with classes containing spaces") {
      val html  = Html(SimplifiedData.FilterClass.ClassWithSpaceInTheName)
      val clazz = html.filter(Class("button login"))
      (clazz should have).length(1)
    }

    it("should return `Nil` when looking for a class that doesn't exists") {
      val html  = Html(SimplifiedData.FilterClass.NestedClasses)
      val clazz = html.filter(Class("no-such-class"))
      clazz shouldBe empty
    }

    it("should filter out the 4 existing `image-section` classes") {
      val html    = Html(RealLifeData.AlbumPageMemberReview)
      val classes = html.filter(Class("image-section"))
      (classes should have).length(4)
    }

    it("should filter out the 9 existing `image-section` classes") {
      val html    = Html(RealLifeData.AlbumPageSetOfTheDay)
      val classes = html.filter(Class("image-section"))
      (classes should have).length(9)
    }

    it("should filter out the 45 existing `photo-container` classes") {
      val html    = Html(RealLifeData.PhotoSetOfTheDay)
      val classes = html.filter(Class("photo-container"))
      (classes should have).length(45)
      classes.zipWithIndex.foreach { pair =>
        val (clazz, index) = pair
        withClue(s"@ index $index ...") {
          clazz should startWith("""<li class="photo-container"""")
        }
      }
    }
  }

  //==========================================================================
  //==========================================================================

  describe("Attribute filter") {
    it("should filter out the 4 existing elements with a `href` attribute") {
      val html  = Html(RealLifeData.AlbumPageMemberReview)
      val hrefs = html.filter(Attribute("href"))
      (hrefs should have).length(4)
      hrefs(0).trim() should startWith("""<a href="href1""")
      hrefs(3).trim() should startWith("""<a href="href4""")
    }

    it("should return `Nil` when looking for an attribute that doesn't exists") {
      val html      = Html(SimplifiedData.FilterTag.NestedTags)
      val attribute = html.filter(Attribute("no-such-attr"))
      attribute shouldBe empty
    }
  }

  //==========================================================================
  //==========================================================================

  describe("grabbing the Values of elements filtered by Attributes") {
    it("should return `data-index`attribute contents") {
      val html      = Html(RealLifeData.PhotoSetOfTheDay)
      val dataIndex = html.filter(Value(Attribute("data-index")))
      (dataIndex should have).length(45)
      assertResult(expected = "0")(dataIndex(0))
      assertResult(expected = "44")(dataIndex(44))
    }
  }

  //==========================================================================
  //==========================================================================

  describe("HrefLink filter") {
    it("should filter out the one link in the html") {
      val html  = Html(SimplifiedData.FilterLink.SingleLink)
      val links = html.filter(HrefLink())
      (links should have).length(1)
      assertResult(expected = "first-link/foo")(links.head)
    }

    it("should filter out the two nested links in the html") {
      val html  = Html(SimplifiedData.FilterLink.NestedLinks)
      val links = html.filter(HrefLink())
      (links should have).length(2)
      assertResult(expected = "first-link/foo")(links(0))
      assertResult(expected = "second-link/foo")(links(1))
    }

    it("should filter out two flat links") {
      val html  = Html(SimplifiedData.FilterLink.FlatLinks)
      val links = html.filter(HrefLink())
      (links should have).length(2)
      assertResult(expected = "first-link/foo")(links(0))
      assertResult(expected = "second-link/foo")(links(1))
    }
  }

  //==========================================================================
  //==========================================================================

  describe("RetainFirst of HrefLink filter") {
    it("should retain only the first link when composed with a RetainFirst filter, on rhs") {
      val html  = Html(SimplifiedData.FilterLink.NestedLinks)
      val links = html.filter(RetainFirst(HrefLink()))
      (links should have).length(1)
      assertResult(expected = "first-link/foo")(links.head)
    }

    it("should retain only the first link when composed with a RetainFirst filter, on lhs") {
      val html  = Html(SimplifiedData.FilterLink.NestedLinks)
      val links = html.filter(RetainFirst(HrefLink()))
      (links should have).length(1)
      assertResult(expected = "first-link/foo")(links.head)
    }
  }

  //==========================================================================
  //==========================================================================

  describe("Content filter") {
    it("should return only the date from the `icon-photography` class") {
      val html    = Html(SimplifiedData.FilterContent.ContentFromClass)
      val content = html.filter(Content(Class("icon-photography")))
      (content should have).length(1)
      assertResult(expected = "Nov 09, 2013")(content.head)
    }

    it("should return only the contents of the `div` tag") {
      val html    = Html(SimplifiedData.FilterContent.ContentFromTag)
      val content = html.filter(Content(Tag("div")))
      (content should have).length(1)
      assertResult(expected = "<a>whatever</a>")(content.head)
    }

    it("should return only the contents of the `id` attribute") {
      val html    = Html(SimplifiedData.FilterContent.ContentFromAttribute)
      val content = html.filter(Content(Attribute("id")))
      (content should have).length(1)
      assertResult(expected = "Load more")(content.head)
    }

    it("should return the contents of a Composite Filter") {
      val html    = Html(SimplifiedData.FilterContent.ContentFromComposite)
      val content = html.filter(Content(Class("meta-data") && Class("photographer")))
      (content should have).length(1)
      content.head.trim() should startWith("by")
    }
  }

  //==========================================================================
  //==========================================================================

  describe("Combining filters") {
    it(
      "should return all the links contained within the `photo-container` classes contained within the first `image-section` class",
    ) {
      val html  = Html(ComplexData.Combination.TwoTopLevelImageSections)
      val links = html.filter(RetainFirst(Class("image-section")) && Class("photo-container") && HrefLink())
      (links should have).length(45)
      assertResult(expected = "link0")(actual  = links(0))
      assertResult(expected = "link44")(actual = links(44))
    }

    it(
      "should return all the links contained within the `photo-container` classes contained within both `image-section` class",
    ) {
      val html  = Html(ComplexData.Combination.TwoTopLevelImageSections)
      val links = html.filter(Class("image-section") && Class("photo-container") && HrefLink())
      (links should have).length(46)
      assertResult(expected = "link0")(actual        = links(0))
      assertResult(expected = "link44")(actual       = links(44))
      assertResult(expected = "BOGUS LINK!!")(actual = links(45))
    }

    it(
      "should return all the links contained within the `photo-container` classes contained within both `image-section` class" +
        " even though the middle `image-section` class contains no photocontainers",
    ) {
      val html  = Html(ComplexData.Combination.ThreeTopLevelImageSectionsMiddleOneEmpty)
      val links = html.filter(Class("image-section") && Class("photo-container") && HrefLink())
      (links should have).length(3)
      assertResult(expected = "link0")(actual        = links(0))
      assertResult(expected = "link44")(actual       = links(1))
      assertResult(expected = "BOGUS LINK!!")(actual = links(2))
    }

    it("should return `Nil` if the first filter in the combination returns `Nil`") {
      val html  = Html(ComplexData.Combination.TwoTopLevelImageSections)
      val links = html.filter(Class("non-existent-class") && Class("photo-container") && HrefLink())
      links shouldBe empty
    }

    it("should return `Nil` if the filter in middle the combination returns `Nil`") {
      val html  = Html(ComplexData.Combination.TwoTopLevelImageSections)
      val links = html.filter(Class("image-section") && Class("non-existent-class") && HrefLink())
      links shouldBe empty
    }

    it("should return `Nil` if the last filter in the combination returns `Nil`") {
      val html  = Html(ComplexData.Combination.TwoTopLevelImageSections)
      val links = html.filter(Class("image-section") && Class("photo-container") && Attribute("non-existent-attribute"))
      links shouldBe empty
    }
  }

  //==========================================================================
  //==========================================================================
}
