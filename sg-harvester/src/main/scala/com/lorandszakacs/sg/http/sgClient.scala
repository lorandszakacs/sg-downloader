package com.lorandszakacs.sg.http

import java.net.URL

import akka.http.scaladsl.model.headers.{HttpOrigin, Origin, RawHeader, Referer}
import akka.http.scaladsl.model.{HttpHeader, HttpRequest}
import com.lorandszakacs.sg.core
import com.lorandszakacs.util.html.Html
import org.joda.time.DateTime
import com.lorandszakacs.util.effects._

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
    * the [[createAuthentication]] method can be used to create such a function, based
    * on username, password authentication
    *
    */
  def getPage(uri: URL)(implicit authentication: Authentication): IO[Html]

  def createAuthentication(session: Session): IO[Authentication]

  /**
    * Website now has google reCAPTCHA, so it's hard to logon, but you can manually create
    * an [[Authentication]] from a [[Session]] using [[createAuthentication]] by pasting
    * the tokens from the browser.
    */
  @scala.deprecated("use createAuthentication", "2018")
  def brokenAuthenticate(username: String, plainTextPassword: String): IO[Authentication]
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
  username:  String,
  sessionID: String,
  csrfToken: String,
  expiresAt: DateTime
) {
  def toCookieHeader: RawHeader = RawHeader("Cookie", s"csrftoken=$csrfToken; sessionid=$sessionID")
}

object DefaultSGAuthentication extends Authentication {
  private val OriginHeader:  Origin  = Origin(HttpOrigin(s"${core.Domain}"))
  private val RefererHeader: Referer = Referer(s"${core.Domain}/")

  private val defaultSGHeaders: Seq[HttpHeader] = Seq(OriginHeader, RefererHeader)

  override def apply(req: HttpRequest): HttpRequest = req.withHeaders(req.headers ++ defaultSGHeaders)

  override def needsRefresh: Boolean = true

  override def session: Session = throw new NotImplementedError("... cannot access session of default authentication")
}
