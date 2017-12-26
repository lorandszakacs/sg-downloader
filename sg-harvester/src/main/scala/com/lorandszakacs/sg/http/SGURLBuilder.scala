package com.lorandszakacs.sg.http

import java.net.URL

import akka.http.scaladsl.model.Uri
import com.lorandszakacs.sg._
import com.lorandszakacs.sg.model._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
trait SGURLBuilder {

  /**
    * This is the URI to the page containing all [[PhotoSet]] of a [[M]]
    */
  def photoSetsPageURL(name: Name): URL =
    new URL(s"${core.Domain}/girls/${name.name}/photos/view/photosets/")

  def makeFullPathURL(uri: String): URL = {
    if (uri.startsWith("/")) {
      new URL(s"${core.Domain}$uri")
    }
    else {
      new URL(s"${core.Domain}/$uri")
    }
  }

  def makeFullPathURL(uri: Uri): URL = {
    makeFullPathURL(uri.toString)
  }
}
