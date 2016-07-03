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
import com.github.nscala_time.time.Imports._
import com.lorandszakacs.sg.model.{Photo, PhotoSet}
import com.lorandszakacs.util.html._

import scala.util._

object SGContentParser {
  private val months = Map(1 -> "Jan", 2 -> "Feb", 3 -> "Mar",
    4 -> "Apr", 5 -> "May", 6 -> "Jun",
    7 -> "Jul", 8 -> "Aug", 9 -> "Sep",
    10 -> "Oct", 11 -> "Nov", 12 -> "Dec").map(p => p._2 -> p._1)

  @scala.deprecated("not as useful as the one that gathers PhotoSet", "now")
  def gatherPhotoSetLinks(html: Html): Try[List[Uri]] = {
    html filter (Tag("header") && Attribute("post_id") && Tag("h2") && Class("title") && RetainFirst(Tag("a")) && HrefLink()) match {
      case Nil => Failure(HTMLPageDidNotContainAnyPhotoSetLinksException(html))
      case links => Success(links.map(Uri(_)))
    }
  }

  /**
    * this filter will yield elements like the following:

    *{{{
    * <header post_id="997826" posttype="album" class="header clearfix">
    * <div class="top-bar">
    * <h2 class="title"> <a href="/girls/moon/album/997826/mirage/">Mirage</a> </h2>
    * <div class="sub-title">
    * <span class="set-type">Set of the day</span>
    * <span class="photographer"> by <a href="/members/cdo/photography/">cdo</a> </span>
    * </div>
    * </div>
    * <div class="sub-header clearfix">
    * <div class="sub-header-container">
    * <div class="meta-data">
    * <time class="time-ago"> Dec 29, 2013 </time>
    * </div>
    * <a id="button-share_997826" type="submit" class="button icon-share has-bar"></a>
    * <a id="likeScore" appname="album" object="album" objectid="997826" direction="clear" class="button like  icon-heart  youLike active">&nbsp;4097</a>
    * </div>
    * <div id="share_997826" class="admin-bar share-links share-menu">
    * <ul class="">
    * <li><a class="facebook-share" href="http://www.suicidegirls.com/girls/moon/album/997826/mirage/" verbose="album" data-facebook-id="508713652527801" data-picture-url="https://d1a0n9gptf7ayu.cloudfront.net/photos/f862054e3afd14f4aaa65dfe19f776ae.jpg" data-name="Mirage" data-description="<div class=&quot;legacy-text&quot;>&amp;lt;div class=&quot;legacy-text&quot;&amp;gt;&amp;lt;/div&amp;gt;</div>">Facebook</a></li>
    * <li><a class="twitter-share" href="http://twitter.com/share?url=http://www.suicidegirls.com/girls/moon/album/997826/mirage/">Tweet</a></li>
    * <li><a class="email-share" href="mailto:?&amp;body=http://www.suicidegirls.com/girls/moon/album/997826/mirage/">Email</a></li>
    * </ul>
    * </div>
    * <div id="edit_997826" class="admin-bar share-links">
    * <ul class="">
    * </ul>
    * </div>
    * </div>
    * </header>
    *}}}
    */
  def gatherPhotoSets(html: Html): Try[List[PhotoSet]] = Try {
    val albumElement = Tag("header") && Attribute("post_id")
    val albumElements = html filter albumElement

    val photoSets: List[PhotoSet] = albumElements map { ae =>
      val html = Html(ae)
      val urlRepr = html filter Tag("h2") && Class("title") && RetainFirst(Tag("a")) && HrefLink()
      val titleRepr = html filter Content(Tag("h2") && Class("title") && RetainFirst(Tag("a")))
      val timeRepr = html filter Content(Tag("time"))
      val url = urlRepr.headOption.getOrElse(throw SetRepresentationDidNotContainURLException(html)).trim()
      val title = titleRepr.headOption.getOrElse(throw SetRepresentationDidNotContainTitleException(html)).trim()
      val date = parseStringToDateTime(timeRepr.headOption.getOrElse(throw SetRepresentationDidNotContainTimeTagException(html)).trim())
      PhotoSet(
        url = url,
        title = title,
        date = date.get,
        photos = Nil
      )
    }
    photoSets
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
    val date: LocalDate = metaData filter Content(Tag("time")) match {
      case Nil => throw new Exception(s"Could not find the date of the album.")
      case l => if (l.length == 1) {
        parseStringToDateTime(l.head).get
      } else throw new Exception(s"Found too many dates for album.")
    }
    val photos = parsePhotos(html) match {
      case Success(ps) => ps
      case Failure(e) => throw new Exception(s"Failed to gather the links for page.", e)
    }
    Success(PhotoSet(url = albumPageUri.toString, title = title, date = date, photos = photos))
  }

  def parsePhotos(albumPage: Html): Try[List[Photo]] = {
    albumPage filter Class("image-section") && Tag("li") && Class("photo-container") && RetainFirst(HrefLink()) match {
      case Nil => throw new Exception(s"Failed to extract any Photo from this document:${albumPage.document.toString}")
      case links => Try(links.zip(1 to links.length).map(pair => Photo(pair._1, pair._2)))
    }
  }

  private def parseStringToDateTime(t: String): Try[LocalDate] = {
    val time = t.trim()
    try {
      //Aug 1, 2012
      val datePattern = """(\w\w\w) (\d*), (\d\d\d\d)""".r
      val datePattern(month, day, year) = time

      val monthAsInt = months(month)

      val dateTime = new LocalDate(year.toInt, monthAsInt, day.toInt)
      Success(dateTime)
    } catch {
      case e: Throwable =>
        try {
          val simplifiedDatePattern = """(\w\w\w) (\d*)""".r
          val simplifiedDatePattern(month, day) = time
          val monthAsInt = months(month)
          val date = new LocalDate(DateTime.now.getYear, monthAsInt, day.toInt)
          Success(date)
        } catch {
          case e: Throwable => Failure(e)
        }
    }
  }
}