package com.lorandszakacs.sgd.client.data

import java.time.LocalDate

import scala.io.Source

import com.lorandszakacs.commons.html._

import spray.http.Uri

trait PhotoSetPage {
  def html = {
    val resourceName = s"${getClass.getSimpleName().replace("$", "")}.html"
    val URL = getClass.getResource(resourceName)
    val source = Source.fromURL(URL)
    Html(source.getLines().mkString("\n"))
  }

  def uri: Uri
  def numberOfPhotos: Int
  def title: String
  def date: LocalDate
}

object PhotoSetPagePartialDate extends PhotoSetPage {
  def uri: Uri = "https://suicidegirls.com/girls/dwam/album/1239337/adieu-tristesse/"
  def numberOfPhotos: Int = 53
  def title: String = "Adieu Tristesse"
  def date: LocalDate = LocalDate.of(2014, 1, 18)
}

object PhotoSetPageFullDate extends PhotoSetPage {
  def uri: Uri = "https://suicidegirls.com/girls/dwam/album/977051/limportance-d-etre-ernest/"
  def numberOfPhotos: Int = 45
  def title: String = "Limportance d etre Ernest"
  def date: LocalDate = LocalDate.of(2013, 2, 7)
}


