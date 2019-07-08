package com.lorandszakacs.sg.http

import java.net.URL

import org.http4s._
import org.http4s.util.CaseInsensitiveString

import com.lorandszakacs.sg.core
import com.lorandszakacs.util.html.Html
import com.lorandszakacs.util.time._
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
  def getPage(uri: URL)(implicit authentication: Authentication): Task[Html]

  def createAuthentication(session: Session): Task[Authentication]

  def cleanup: Task[Unit]

  /**
    * Website now has google reCAPTCHA, so it's hard to logon, but you can manually create
    * an [[Authentication]] from a [[Session]] using [[createAuthentication]] by pasting
    * the tokens from the browser.
    */
  //@scala.deprecated("use createAuthentication", "2018")
  //def brokenAuthenticate(username: String, plainTextPassword: String): Task[Authentication]
}

trait Authentication {
  def apply(req: Request[Task]): Request[Task]

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
  expiresAt: Instant,
) {

  def toCookieHeader: Header =
    Header.Raw(CaseInsensitiveString("Cookie"), s"csrftoken=$csrfToken; sessionid=$sessionID")
}

object DefaultSGAuthentication extends Authentication {
  private val OriginHeader:  Header = Header.Raw(CaseInsensitiveString("Origin"), s"${core.Domain}")
  private val RefererHeader: Header = Header.Raw(CaseInsensitiveString("Referer"), s"${core.Domain}/")

  private val defaultSGHeaders: List[Header] = List(OriginHeader, RefererHeader)

  override def apply(req: Request[Task]): Request[Task] = req.putHeaders(defaultSGHeaders: _*)

  override def needsRefresh: Boolean = true

  override def session: Session = throw new NotImplementedError("... cannot access session of default authentication")
}
