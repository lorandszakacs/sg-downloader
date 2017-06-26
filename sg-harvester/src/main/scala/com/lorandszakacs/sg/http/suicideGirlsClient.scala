package com.lorandszakacs.sg.http

import java.net.URL

import akka.http.scaladsl.model.headers.{HttpOrigin, Origin, RawHeader, Referer}
import akka.http.scaladsl.model.{HttpHeader, HttpRequest}
import com.lorandszakacs.util.html.Html
import org.joda.time.DateTime

import scala.concurrent.Future

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
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
  def getPage(uri: URL)(implicit authentication: Authentication): Future[Html]

  def authenticate(username: String, plainTextPassword: String): Future[Authentication]

  def createAuthentication(session: Session): Future[Authentication]
}

trait Authentication {
  def apply(req: HttpRequest): HttpRequest

  def needsRefresh: Boolean

  def session: Session
}

/**
  * Session contains all the information necessary to create an authentication
  */
final case class Session(
  username: String,
  sessionID: String,
  csrfToken: String,
  expiresAt: DateTime
) {
  def toCookieHeader: RawHeader = RawHeader("Cookie", s"csrftoken=$csrfToken; sessionid=$sessionID")
}


object DefaultSGAuthentication extends Authentication {
  private val OriginHeader: Origin = Origin(HttpOrigin("https://www.suicidegirls.com"))
  private val RefererHeader: Referer = Referer("https://www.suicidegirls.com/")

  private val defaultSGHeaders: Seq[HttpHeader] = Seq(OriginHeader, RefererHeader)

  override def apply(req: HttpRequest): HttpRequest = req.withHeaders(req.headers ++ defaultSGHeaders)

  override def needsRefresh: Boolean = true

  override def session: Session = throw new NotImplementedError("... cannot access session of default authentication")
}