package com.lorandszakacs.sgd.http

import scala.util.Try
import com.lorandszakacs.sgd.model.PhotoSetShallow
import scala.util.Success
import scala.util.Failure
import com.lorandszakacs.commons.html._
import java.time.LocalDate
import com.lorandszakacs.sgd.model.PhotoShallow
import spray.http.Uri

object Parser {
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