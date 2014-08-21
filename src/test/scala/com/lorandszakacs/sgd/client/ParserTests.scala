package com.lorandszakacs.sgd.client

import scala.util.{ Failure, Success }

import org.scalatest.{ FlatSpec, Matchers }

import com.lorandszakacs.sgd.client.data._
import com.lorandszakacs.sgd.http.Parser

class ParserTests extends FlatSpec with Matchers {

  behavior of "Parser"

  it should "return all the links from a PhotoSetPage" in {
    val expected = PhotoSetPageFullDate
    Parser.parsePhotos(expected.html) match {
      case Success(result) => result should have length expected.numberOfPhotos
      case Failure(e) => fail("did not return any photos", e)
    }
  }

  it should "return a PhotoSet object from a page with a full date" in {
    val expected = PhotoSetPageFullDate
    Parser.parsePhotoSetPage(expected.html, expected.uri) match {
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
    Parser.parsePhotoSetPage(expected.html, expected.uri) match {
      case Success(result) =>
        result.photos should have length expected.numberOfPhotos
        result.date should equal(expected.date)
        result.title should equal(expected.title)
      case Failure(e) =>
        fail("did not return any photos", e)
    }
  }

  it should "return all PhotoSet URLs from a SG page" in {
    val expected = SGSetPage
    Parser.gatherPhotoSetLinks(expected.html) match {
      case Success(result) =>
        result should have length expected.numberOfPhotoSets
        result.diff(expected.photoSetURIs) should be(Nil)
      case Failure(e) =>
        fail("did not return any PhotoSetLinks", e)
    }
  }
}