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
package com.lorandszakacs.sg.contentparser
import com.lorandszakacs.util.effects._
import com.lorandszakacs.sg.contentparser.data._
import org.scalatest.{FlatSpec, Matchers}

/**
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 16 Mar 2015
  *
  */
class SGPageContentParserTests extends FlatSpec with Matchers {

  behavior of "SGContentParser"

  //===============================================================================================

  it should "return all the links from a PhotoSetPage" in {
    val expected = PhotoSetPageFullDate
    SGContentParser.parsePhotos(expected.html) match {
      case TrySuccess(result) =>
        result should have length expected.numberOfPhotos.toLong
        val last = result.last
        assert(last.index == expected.numberOfPhotos - 1, "... index")
        assert(
          last.url.toExternalForm == "https://example.com/sample_image_aaaaaa.jpg",
          "... url"
        )
        assert(
          last.thumbnailURL.toExternalForm == "https://example.com/sample_image_bbbbbb.jpg",
          "... thumbnail url"
        )
      case TryFailure(e) => fail("did not return any photos", e)
    }
  }

  //===============================================================================================

  it should "return all PhotoSets from a SG page" in {
    val expected = SGSetPageAllInPast
    SGContentParser.gatherPhotoSetsForM(expected.html) match {
      case TrySuccess(result) =>
        result should have length expected.numberOfPhotoSets.toLong
        result.diff(expected.photoSets) should be(Nil)
      case TryFailure(e) =>
        fail("did not return any PhotoSetLinks", e)
    }
  }

  //===============================================================================================

  it should "return all PhotoSets from a SG page, in which all sets are in the past" in {
    val expected = SGSetPageSomeInPast
    SGContentParser.gatherPhotoSetsForM(expected.html) match {
      case TrySuccess(result) =>
        result should have length expected.numberOfPhotoSets.toLong
        result.head should equal(expected.photoSets.head)
        result.diff(expected.photoSets) should equal(Nil)
      case TryFailure(e) =>
        fail("did not return any PhotoSetLinks", e)
    }
  }

  //===============================================================================================

  it should "return all the SG names from the profile listing page" in {
    val expected = SGProfileListPage
    SGContentParser.gatherSGNames(expected.html) match {
      case TrySuccess(result) =>
        result should have length expected.numberOfSGs.toLong
        result should equal(expected.names)
      case TryFailure(e) => fail("did not return any SG Names", e)
    }
  }

  //===============================================================================================

  it should "return all the HF Names from the profile listing page" in {
    val expected = HFProfileListPage
    SGContentParser.gatherHFNames(expected.html) match {
      case TrySuccess(result) =>
        result should have length expected.numberOfSGs.toLong
        result should equal(expected.names)
      case TryFailure(e) => fail("did not return any SG Names", e)
    }
  }

  //===============================================================================================

  it should "return all the Ms from the newest photos page" in {
    val expected = NewestPhotosPageWithDoubleMSet
    SGContentParser.gatherNewestPhotoSets(expected.html) match {
      case TrySuccess(result) =>
        result should have length expected.numberOfMs.toLong
        result.head.photoSetURL should equal(expected.ms.head.photoSetURL)
        result.head.name should equal(expected.ms.head.name)
        result.head.photoSets should equal(expected.ms.head.photoSets)

        result.head should equal(expected.ms.head)
        val r1 = result.take(4).sortBy(_.name)
        val r2 = expected.ms.sortBy(_.name)
        r1 should equal(r2)

      case TryFailure(e) => fail("did not return any Ms", e)
    }
  }

  //===============================================================================================

}
