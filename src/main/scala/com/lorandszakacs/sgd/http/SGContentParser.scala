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
package com.lorandszakacs.sgd.http

import scala.util._
import com.lorandszakacs.util.html._
import com.github.nscala_time.time.Imports._
import com.lorandszakacs.sgd.model._
import spray.http.Uri

object SGContentParser {
  def gatherPhotoSetLinks(html: Html): Try[List[Uri]] = {
    html filter (Tag("header") && Attribute("post_id") && Tag("h2") && Class("title") && RetainFirst(Tag("a")) && HrefLink()) match {
      case Nil => Failure(new Exception("Did not find any PhotoSet links."))
      case links => Success(links.map(Uri(_)))
    }
  }

  def gatherSGNames(html: Html): Try[List[String]] = {
    html filter (Class("image-section") && RetainFirst(Tag("a")) && HrefLink()) match {
      case Nil => Failure(new Exception("Did not find any Profile links."))
      case links => Success(links.map(_.replace("/girls/", "").replace("/", "").capitalize))
    }
  }

  def gatherHopefulNames(html: Html): Try[List[String]] = {
    html filter (Class("image-section") && RetainFirst(Tag("a")) && HrefLink()) match {
      case Nil => Failure(new Exception("Did not find any Profile links."))
      case links => Success(links.map(_.replace("/members/", "").replace("/", "").capitalize))
    }
  }

  def parsePhotoSetPage(html: Html, albumPageUri: Uri): Try[PhotoSet] = {
    //article-feed album-view clearfix
    val metaData: Html = html filter RetainFirst(Class("content-box")) match {
      case Nil => throw new Exception(s"Could not find album meta-data.")
      case l => Html(l.head)
    }
    val title: String = metaData filter Content(Class("title")) match {
      case Nil => throw new Exception(s"Could not find the title of the album.")
      case l => if (l.length == 1) l.head.trim() else throw new Exception(s"Found too many titles for album.")
    }
    val date: DateTime = metaData filter Content(Tag("time")) match {
      case Nil => throw new Exception(s"Could not find the date of the album.")
      case l => if (l.length == 1) {
        parseStringToDateTime(l.head).get
      } else throw new Exception(s"Found too many dates for album.")
    }
    val photos = parsePhotos(html) match {
      case Success(ps) => ps
      case Failure(e) => throw new Exception(s"Failed to gather the links for page.", e)
    }
    Success(PhotoSet(uri = albumPageUri, title = title, photos = photos, date = date))
  }

  def parsePhotos(albumPage: Html): Try[List[Photo]] = {
    albumPage filter Class("image-section") && Tag("li") && Class("photo-container") && RetainFirst(HrefLink()) match {
      case Nil => throw new Exception(s"Failed to extract any Photo from this document:${albumPage.document.toString}")
      case links => Try(links.zip(1 to links.length).map(pair => Photo(pair._1, pair._2)))
    }
  }

  private val months = Map(1 -> "Jan", 2 -> "Feb", 3 -> "Mar",
    4 -> "Apr", 5 -> "May", 6 -> "Jun",
    7 -> "Jul", 8 -> "Aug", 9 -> "Sep",
    10 -> "Oct", 11 -> "Nov", 12 -> "Dec").map(p => p._2 -> p._1)

  private def parseStringToDateTime(t: String): Try[DateTime] = {
    val time = t.trim()
    try {
      //Aug 1, 2012
      val datePattern = """(\w\w\w) (\d*), (\d\d\d\d)""".r
      val datePattern(month, day, year) = time

      val monthAsInt = months(month)

      val dateTime = new DateTime(DateTimeZone.UTC)
                     .withDate(year.toInt, monthAsInt, day.toInt)
                     .withTimeAtStartOfDay()

      Success(dateTime)
    } catch {
      case e: Throwable =>
        try {
          val simplifiedDatePattern = """(\w\w\w) (\d*)""".r
          val simplifiedDatePattern(month, day) = time
          val monthAsInt = months(month)
          val dateTime = new DateTime(DateTimeZone.UTC)
                         .withDate(DateTime.now.getYear, monthAsInt, day.toInt)
                         .withTimeAtStartOfDay()
          Success(dateTime)
        } catch {
          case e: Throwable => Failure(e)
        }
    }
  }
}