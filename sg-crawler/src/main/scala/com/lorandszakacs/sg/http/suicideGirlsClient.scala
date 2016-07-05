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

  /**
    * @param authentication
    * the function that adds some authentication or another to requests.
    * the [[authenticate()]] method can be used to create such a function, based
    * on username, password authentication
    *
    */
  def getPage(uri: Uri)(implicit authentication: Authentication): Future[Html]

  def authenticate(username: String, plainTextPassword: String): Future[Authentication]

}

trait Authentication {
  def apply(req: HttpRequest): HttpRequest

  def needsRefresh: Boolean
}

object IdentityAuthentication extends Authentication {
  override def apply(req: HttpRequest): HttpRequest = req

  override def needsRefresh: Boolean = true
}