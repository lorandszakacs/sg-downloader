package com.lorandszakacs.sg.http

import akka.http.scaladsl.model.headers.{HttpOrigin, Origin, Referer}
import akka.http.scaladsl.model.{HttpHeader, HttpRequest, Uri}
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

object DefaultSGAuthentication extends Authentication {
  private val OriginHeader: Origin = Origin(HttpOrigin("https://www.suicidegirls.com"))
  private val RefererHeader: Referer = Referer("https://www.suicidegirls.com/")

  private val defaultSGHeaders: Seq[HttpHeader] = Seq(OriginHeader, RefererHeader)

  override def apply(req: HttpRequest): HttpRequest = req.withHeaders(req.headers ++ defaultSGHeaders)

  override def needsRefresh: Boolean = true
}