package com.lorandszakacs.sg.http

import java.net.URL

import akka.http.scaladsl.model.Uri
import com.lorandszakacs.sg.model._

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
trait SGURLBuilder {
  /**
    * This is the URI to the page containing all [[PhotoSet]] of a [[Model]]
    *
    * It is prefixed with "girls" instead of "members" even for hopefuls
    */
  def photoSetsPageURL(modelName: ModelName): URL =
    new URL(s"https://www.suicidegirls.com/girls/${modelName.name}/photos/view/photosets/")

  def makeFullPathURL(uri: String): URL = {
    if (uri.startsWith("/")) {
      new URL(s"https://www.suicidegirls.com$uri")
    } else {
      new URL(s"https://www.suicidegirls.com/$uri")
    }
  }

  def makeFullPathURL(uri: Uri): URL = {
    makeFullPathURL(uri.toString)
  }
}