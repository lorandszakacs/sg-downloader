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
package com.lorandszakacs.sg.crawler.page

import akka.http.scaladsl.model.Uri
import com.lorandszakacs.sg.crawler.page.data._
import org.scalatest.{FlatSpec, Matchers}

import scala.util.{Failure, Success}

/**
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 16 Mar 2015
  *
  */
class SGPageContentParserTests extends FlatSpec with Matchers {

  behavior of "SGContentParser"

  it should "return all the links from a PhotoSetPage" in {
    val expected = PhotoSetPageFullDate
    SGContentParser.parsePhotos(expected.html) match {
      case Success(result) => result should have length expected.numberOfPhotos
      case Failure(e) => fail("did not return any photos", e)
    }
  }

  it should "return a PhotoSet object from a page with a full date" in {
    val expected = PhotoSetPageFullDate
    SGContentParser.parsePhotoSetPage(expected.html, expected.uri) match {
      case Success(result) =>
        result.photos should have length expected.numberOfPhotos
        result.date should equal(expected.date)
        result.title should equal(expected.title)
      case Failure(e) =>
        fail("did not return any photos", e)
    }
  }

  it should "return a PhotoSet object from a page with a partial date" in {
    val expected = PhotoSetPagePartialDate
    SGContentParser.parsePhotoSetPage(expected.html, expected.uri) match {
      case Success(result) =>
        result.photos should have length expected.numberOfPhotos
        result.date should equal(expected.date)
        result.title should equal(expected.title)
      case Failure(e) =>
        fail("did not return any photos", e)
    }
  }

  it should "return all PhotoSets from a SG page" in {
    val expected = SGSetPageAllInPast
    SGContentParser.gatherPhotoSets(expected.html) match {
      case Success(result) =>
        result should have length expected.numberOfPhotoSets
        result.diff(expected.photoSets) should be(Nil)
      case Failure(e) =>
        fail("did not return any PhotoSetLinks", e)
    }
  }

  it should "return all PhotoSets from a SG page, in which all sets are in the past" in {
    val expected = SGSetPageSomeInPast
    SGContentParser.gatherPhotoSets(expected.html) match {
      case Success(result) =>
        result should have length expected.numberOfPhotoSets
        result.diff(expected.photoSets) should be(Nil)
      case Failure(e) =>
        fail("did not return any PhotoSetLinks", e)
    }
  }

  it should "return all the SG names from the profile listing page" in {
    val expected = SGProfileListPage
    SGContentParser.gatherSGNames(expected.html) match {
      case Success(result) =>
        result should have length expected.numberOfSGs
        result should equal(expected.names)
      case Failure(e) => fail("did not return any SG Names", e)
    }
  }

  it should "return all the HopefulNames from the profile listing page" in {
    val expected = HopefulProfileListPage
    SGContentParser.gatherHopefulNames(expected.html) match {
      case Success(result) =>
        result should have length expected.numberOfSGs
        result should equal(expected.names)
      case Failure(e) => fail("did not return any SG Names", e)
    }
  }

}