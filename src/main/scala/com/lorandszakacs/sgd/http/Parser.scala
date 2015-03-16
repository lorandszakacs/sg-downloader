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
package com.lorandszakacs.sgd.http

import scala.util.Try
import com.lorandszakacs.sgd.model.PhotoSetShallow
import scala.util.Success
import scala.util.Failure
import com.lorandszakacs.util.html._
import java.time.LocalDate
import com.lorandszakacs.sgd.model.PhotoShallow
import spray.http.Uri

object Parser {
  def gatherPhotoSetLinks(html: Html): Try[List[Uri]] = {
    html filter (Tag("header") && Attribute("post_id") && Tag("h2") && Class("title") && RetainFirst(Tag("a")) && HrefLink()) match {
      case None => Failure(new Exception("Did not find any PhotoSet links."))
      case Some(links) => Success(links.map(Uri(_)))
    }
  }

  def gatherSGNames(html: Html): Try[List[String]] = {
    html filter (Class("image-section") && RetainFirst(Tag("a")) && HrefLink()) match {
      case None => Failure(new Exception("Did not find any Profile links."))
      case Some(links) => Success(links.map(_.replace("/girls/", "").replace("/", "").capitalize))
    }
  }
  
  def gatherHopefulNames(html: Html): Try[List[String]] = {
    html filter (Class("image-section") && RetainFirst(Tag("a")) && HrefLink()) match {
      case None => Failure(new Exception("Did not find any Profile links."))
      case Some(links) => Success(links.map(_.replace("/members/", "").replace("/", "").capitalize))
    }
  }

  def parsePhotoSetPage(html: Html, albumPageUri: Uri): Try[PhotoSetShallow] = {
    //article-feed album-view clearfix
    val metaData: Html = (html filter RetainFirst(Class("content-box"))) match {
      case None => throw new Exception(s"Could not find album meta-data.")
      case Some(l) => Html(l.head)
    }
    val title: String = (metaData filter Content(Class("title"))) match {
      case None => throw new Exception(s"Could not find the title of the album.")
      case Some(l) => if (l.length == 1) l.head.trim() else throw new Exception(s"Found too many titles for album.")
    }
    val date: LocalDate = (metaData filter Content(Tag("time"))) match {
      case None => throw new Exception(s"Could not find the date of the album.")
      case Some(l) => if (l.length == 1) { parseStringToLocalDate(l.head).get } else throw new Exception(s"Found too many dates for album.")
    }
    val photos = parsePhotos(html) match {
      case Success(ps) => ps
      case Failure(e) => throw new Exception(s"Failed to gather the links for page.", e)
    }
    Success(PhotoSetShallow(albumPageUri, title, photos, date))
  }

  def parsePhotos(albumPage: Html): Try[List[PhotoShallow]] = {
    albumPage filter Class("image-section") && Tag("li") && Class("photo-container") && RetainFirst(HrefLink()) match {
      case Some(links) => Try(links.zip(1 to links.length).map(pair => PhotoShallow(pair._1, pair._2)))
      case None => throw new Exception(s"Failed to extract any Photo from this document:${albumPage.document.toString}")
    }
  }

  private val months = Map(1 -> "Jan", 2 -> "Feb", 3 -> "Mar",
    4 -> "Apr", 5 -> "May", 6 -> "Jun",
    7 -> "Jul", 8 -> "Aug", 9 -> "Sep",
    10 -> "Oct", 11 -> "Nov", 12 -> "Dec").map(p => p._2 -> p._1)

  private def parseStringToLocalDate(t: String): Try[LocalDate] = {
    val time = t.trim()
    try {
      //Aug 1, 2012
      val datePattern = """(\w\w\w) (\d*), (\d\d\d\d)""".r
      val datePattern(month, day, year) = time

      val monthAsInt = months(month)

      val localDate = LocalDate.of(year.toInt, monthAsInt, day.toInt)
      Success(localDate)
    } catch {
      case e: Throwable => {
        try {
          val simplifiedDatePattern = """(\w\w\w) (\d*)""".r
          val simplifiedDatePattern(month, day) = time
          val monthAsInt = months(month)
          val localDate = LocalDate.of(LocalDate.now().getYear(), monthAsInt, day.toInt)
          Success(localDate)
        } catch {
          case e: Throwable => Failure(e)
        }
      }
    }
  }
}