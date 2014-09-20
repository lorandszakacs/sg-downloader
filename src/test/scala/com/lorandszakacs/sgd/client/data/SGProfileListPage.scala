package com.lorandszakacs.sgd.client.data

import scala.io.Source
import com.lorandszakacs.commons.html.Html
import spray.http.Uri

object SGProfileListPage {
  def html = {
    val resourceName = s"${getClass.getSimpleName().replace("$", "")}.html"
    val URL = getClass.getResource(resourceName)
    val source = Source.fromURL(URL)
    Html(source.getLines().mkString("\n"))
  }

  val names = List("Sash",
    "Kemper",
    "Radeo",
    "Lass",
    "Quinne",
    "Riae",
    "Dimples",
    "Mel",
    "Phecda",
    "Annalee",
    "Bully",
    "Rambo")

  val numberOfSGs = names.length
}