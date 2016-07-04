package com.lorandszakacs.sg.http

import akka.http.scaladsl.model.{HttpRequest, Uri}
import com.lorandszakacs.util.html.Html

import scala.concurrent.Future

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
trait SGClient {
  def getPage(uri: Uri): Future[Html]

  def authenticate(username: String, plainTextPassword: String): Future[(HttpRequest => HttpRequest)]
}