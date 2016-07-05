package com.lorandszakacs.sg.http

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
  def photoSetsPageURL(modelName: ModelName): Uri =
    Uri(s"https://www.suicidegirls.com/girls/${modelName.name}/photos/view/photosets/")

  def makeFullPathURI(uri: String): Uri = {
    if (uri.startsWith("/")) {
      Uri(s"https://www.suicidegirls.com$uri")
    } else {
      Uri(s"https://www.suicidegirls.com/$uri")
    }
  }

  def makeFullPathURI(uri: Uri): Uri = {
    makeFullPathURI(uri.toString)
  }
}
