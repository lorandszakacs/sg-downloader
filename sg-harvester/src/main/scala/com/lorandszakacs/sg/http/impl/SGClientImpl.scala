package com.lorandszakacs.sg.http.impl

/**
  * Copyright 2015 Lorand Szakacs
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
import java.net.URL

import akka.actor.ActorSystem
import akka.http.scaladsl._
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.lorandszakacs.sg.core
import com.lorandszakacs.sg.http._
import com.lorandszakacs.util.html._
import com.lorandszakacs.util.future._
import org.joda.time.DateTimeZone

import scala.collection.immutable.Seq
import com.lorandszakacs.util.future._

import scala.util.Try

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 03 Jul 2016
  *
  */
private[http] object SGClientImpl {
  private[http] def apply()(implicit actorSystem: ActorSystem, ec: ExecutionContext) = new SGClientImpl()
}

private[impl] final class SGClientImpl private ()(implicit val actorSystem: ActorSystem, val ec: ExecutionContext)
    extends SGClient {

  private val http:                  HttpExt           = Http()
  private implicit val materializer: ActorMaterializer = ActorMaterializer()

  override def getPage(url: URL)(implicit authentication: Authentication): Future[Html] = {
    val req         = get(url)
    val reqWithAuth = authentication(req)

    for {
      response <- http.singleRequest(reqWithAuth)
      body <- if (response.status == StatusCodes.OK || response.status == StatusCodes.NotModified) {
               response.entityAsString
             }
             else {
               Future.failed(FailedToGetPageException(url, req, response))
             }
      html = Html(body)
    } yield html
  }

  /**
    * Steps:
    * ------------------------------------------
    * 1. GET ``$domain/``
    *
    * we receive headers:
    *
    * {{{
    *   Set-Cookie: csrftoken=ntk89cZcgo7hynvSMpDMdYxW75hIjo1Z; expires=Mon, 03-Jul-2017 15:44:51 GMT; Max-Age=31449600; Path=/
    *   Set-Cookie: sessionid="gAJ9cQEoVQJhZE5VCmdlbmVyaWNfYWROdS4:1bK63H:i1FhbU7q9BwYx5pXybGPe4V32u0"; expires=Mon, 18-Jul-2016 15:44:51 GMT; httponly; Max-Age=1209600; Path=/
    * }}}
    * let ``CSRF``=ntk89cZcgo7hynvSMpDMdYxW75hIjo1Z
    * let ``SessionID``="gAJ9cQEoVQJhZE5VCmdlbmVyaWNfYWROdS4:1bK63H:i1FhbU7q9BwYx5pXybGPe4V32u0"
    *
    * ------------------------------------------
    * 2. POST ``$domain/login``
    * we add headers:
    * {{{
    *   Origin: $domain
    *   Referer: $domain/
    *   Cookie: csrftoken=``$CSRF``; sessionid=``$SessionID``
    *   X-CSRFToken: $CSRF
    *   Form Data: csrfmiddlewaretoken=``$CSRF``&username=``$username``&password=``$plainTextPassword``
    * }}}
    *
    * we receive:
    * {{{
    *   Set-Cookie: sessionid=".eJxNjk0LgkAURc0-FkEE_QohkJy0xm2ta_eg3fCc90alUsZxlkE_PQsXbc8993Lf4ctOIgiRrrBR6PtKecedKlDfuSEbwtaVXxz_shG7-Oxd3z5hUC8t8eM06lNY_41U6Co7g4iYkx3mB8FaIrJMiI4ayejCYIokiVOTCTKw-ivXZOe3RRAEmcjFXsKy5Ia7Wqvhqo8_X1U-tQ:1bK680:Gzp8vYSJUzkHmFyv46LiZwE3jmk"; expires=Mon, 18-Jul-2016 15:49:44 GMT; httponly; Max-Age=1209600; Path=/
    *   Set-Cookie: csrftoken=T89gMiyIWF5KioMB56wvoZTvOXta6YXp; expires=Mon, 03-Jul-2017 15:49:44 GMT; Max-Age=31449600; Path=/
    * }}}
    * let ``FinalSessionID`` = ".eJxNjk0LgkAURc0-FkEE_QohkJy0xm2ta_eg3fCc90alUsZxlkE_PQsXbc8993Lf4ctOIgiRrrBR6PtKecedKlDfuSEbwtaVXxz_shG7-Oxd3z5hUC8t8eM06lNY_41U6Co7g4iYkx3mB8FaIrJMiI4ayejCYIokiVOTCTKw-ivXZOe3RRAEmcjFXsKy5Ia7Wqvhqo8_X1U-tQ:1bK680:Gzp8vYSJUzkHmFyv46LiZwE3jmk"
    * let ``FinalCSRFToken`` = T89gMiyIWF5KioMB56wvoZTvOXta6YXp
    *
    *
    * ------------------------------------------
    * 3. On any subsequent request we include the following headers:
    * {{{
    *   Referer: $domain/
    *   Cookie: sessionid=``$FinalSessionID``; csrftoken=``$FinalCSRFToken``
    * }}}
    */
  def authenticate(username: String, plainTextPassword: String): Future[Authentication] = {
    case class StartPageTokens(
      csrfToken: String,
      sessionID: String
    ) {
      def toCookieHeader: RawHeader = RawHeader("Cookie", s"csrftoken=$csrfToken; sessionid=$sessionID")
    }

    /**
      * Needed because SG sends us bullshit, apparently:
      * {{{
      * Exception in thread "main" java.lang.IllegalArgumentException: requirement failed: ';' not allowed in cookie content ('sessionid=gAJ9cQEoVQJhZE5VCmdlbmVyaWNfYWROdS4:1bKQ7K:cQ3Lq5uJ35Ynw6HuBsaa2hX2o3E; Expires=Tue, 19 Jul 2016 13:10:22 GMT; Max-Age=1209600; Path=/; HttpOnly')
      * }}}
      */
    def sanitizeCookiesValue(value: String): String = {
      val firstSemicolonIndex = value.indexOf(";")
      if (firstSemicolonIndex >= 0) {
        value.take(firstSemicolonIndex)
      }
      else value
    }

    def splitAtEqualChar(string: String): Option[(String, String)] = {
      val split = string.split("=")
      if (split.length != 2) {
        None
      }
      else
        Option {
          (split(0), split(1))
        }
    }

    def getTokensFromStartPage: Future[StartPageTokens] = {
      val getRequest = HttpRequest(
        method = GET,
        uri    = s"${core.Domain}/"
      )
      for {
        response <- http.singleRequest(getRequest)
        result <- if (response.status != StatusCodes.OK) {
                   Future.failed(FailedToGetSGHomepageOnLoginException(getRequest.uri, response.status))
                 }
                 else {
                   val headers = response._2

                   /**
                     * At this point in time the cookies will look like this:
                     * {{{
                     *     Set-Cookie=sessionid=gAJ9cQEoVQpnZW5lcmljX2FkTlUCYWROdS4:1bKQBZ:4OAMJTBA81esAagrd-pokYdyZq8
                     *     Set-Cookie=csrftoken=ntk89cZcgo7hynvSMpDMdYxW75hIjo1Z
                     * }}}
                     */
                   val wrongCookies: Seq[HttpCookie] = headers
                     .filter(_.is("set-cookie"))
                     .map(c => HttpCookiePair(c.name(), sanitizeCookiesValue(c.value())).toCookie())
                   if (wrongCookies.length != 2) {
                     Future.failed(ExpectedTwoSetCookieHeadersFromHomepage(getRequest.uri, headers))
                   }
                   else {

                     /**
                       * Will look like:
                       * {{{
                       *   (sessionid, gAJ9cQEoVQpnZW5lcmljX2FkTlUCYWROdS4:1bKQBZ:4OAMJTBA81esAagrd-pokYdyZq8)
                       *   (csrftoken, ntk89cZcgo7hynvSMpDMdYxW75hIjo1Z)
                       * }}}
                       */
                     val rawCookies: Seq[(String, String)] = wrongCookies flatMap (c => splitAtEqualChar(c.value))
                     val tokens = for {
                       csrfToken <- Try(
                                     rawCookies
                                       .find(_._1 == "csrftoken")
                                       .getOrElse(throw ExpectedCSRFTokenOnSGHomepageException(getRequest.uri))
                                       ._2
                                   )
                       sessionId <- Try(
                                     rawCookies
                                       .find(_._1 == "sessionid")
                                       .getOrElse(throw ExpectedSessionIdSGHomepageException(getRequest.uri))
                                       ._2
                                   )
                     } yield
                       StartPageTokens(
                         csrfToken = csrfToken,
                         sessionID = sessionId
                       )
                     Future fromTry tokens
                   }
                 }
      } yield result
    }

    def postLoginAndGetTokens(tokens: StartPageTokens): Future[Session] = {
      val headers: Seq[HttpHeader] = Seq(
        tokens.toCookieHeader,
        RawHeader("X-CSRFToken", tokens.csrfToken)
      )
      val entity = FormData
        .apply(
          "csrfmiddlewaretoken" -> tokens.csrfToken,
          "username"            -> username,
          "password"            -> plainTextPassword
        )
        .toEntity(HttpCharsets.`UTF-8`)

      val loginRequest = post(
        uri     = s"${core.Domain}/login/",
        headers = headers,
        entity  = entity
      )

      for {
        response <- http.singleRequest(DefaultSGAuthentication(loginRequest))
        tokens <- if (response.status != StatusCodes.Created) {
                   Future.failed(FailedToPostLoginException(loginRequest, response))
                 }
                 else {
                   val headers = response._2

                   /**
                     * At this point in time the cookies will look like this:
                     * {{{
                     *     Set-Cookie=sessionid=gAJ9cQEoVQpnZW5lcmljX2FkTlUCYWROdS4:1bKQBZ:4OAMJTBA81esAagrd-pokYdyZq8
                     *     Set-Cookie=csrftoken=ntk89cZcgo7hynvSMpDMdYxW75hIjo1Z
                     * }}}
                     */
                   val wrongCookies: Seq[HttpCookie] = headers
                     .filter(_.is("set-cookie"))
                     .map(c => HttpCookiePair(c.name(), sanitizeCookiesValue(c.value())).toCookie())
                   if (wrongCookies.length != 2) {
                     Future.failed(ExpectedTwoSetCookieHeadersFromLoginResponseException(loginRequest.uri, headers))
                   }
                   else {

                     /**
                       * Will look like:
                       * {{{
                       *   (sessionid, gAJ9cQEoVQpnZW5lcmljX2FkTlUCYWROdS4:1bKQBZ:4OAMJTBA81esAagrd-pokYdyZq8)
                       *   (csrftoken, ntk89cZcgo7hynvSMpDMdYxW75hIjo1Z)
                       * }}}
                       */
                     val rawCookies: Seq[(String, String)] = wrongCookies flatMap (c => splitAtEqualChar(c.value))
                     val tokens = for {
                       csrfToken <- Try(
                                     rawCookies
                                       .find(_._1 == "csrftoken")
                                       .getOrElse(throw ExpectedCSRFTokenOnSGLoginResponseException(loginRequest.uri))
                                       ._2
                                   )
                       sessionId <- Try(
                                     rawCookies
                                       .find(_._1 == "sessionid")
                                       .getOrElse(throw ExpectedSessionIdSGLoginResponseException(loginRequest.uri))
                                       ._2
                                   )
                     } yield
                       Session(
                         username  = username,
                         sessionID = sessionId,
                         csrfToken = csrfToken,
                         expiresAt = org.joda.time.DateTime.now(DateTimeZone.UTC).plusDays(13)
                       )
                     Future fromTry tokens
                   }
                 }

      } yield tokens
    }

    for {
      initialTokens: StartPageTokens <- getTokensFromStartPage
      newSession:    Session         <- postLoginAndGetTokens(initialTokens)
      newAuthentication = authenticationFromSession(newSession)
      _ <- verifyAuthentication(newAuthentication)
    } yield newAuthentication
  }

  override def createAuthentication(newSession: Session): Future[Authentication] = {
    val newAuthentication = authenticationFromSession(newSession)
    for {
      _ <- verifyAuthentication(newAuthentication)
    } yield newAuthentication
  }

  private def authenticationFromSession(newSession: Session): Authentication = {
    new Authentication {
      override def needsRefresh: Boolean = false

      override def apply(req: HttpRequest): HttpRequest = {
        val newHeaders = req.headers :+ newSession.toCookieHeader
        req.withHeaders(newHeaders)
      }

      override val session: Session = newSession
    }
  }

  /**
    * Simply tries to go to:
    * $domain/members/$username/
    *
    * and verify that the login button is no longer there. HTML looks like the following:
    * {{{
    *      <div id="login">
    *        <a class="button login">Login</a>
    *
    *        <div id="login-wrapper">
    *          <div id="login-bg"></div>
    *          <div class="login-form-wrapper">
    *            <form id="login-form" method="post" action="/login/"><input type='hidden' name='csrfmiddlewaretoken' value='0oNlDRsv9EHHr5jhfYW5OZIUYcD6ca8V' />
    *              <div>
    *                <div><!-- we built div city! -->
    *                  <div class="errors"></div>
    *                  <input id="username" autocapitalize="off" name="username" maxlength="254" />
    *                  <input type="password" name="password" maxlength="100" />
    *                  <button type="submit" class="button call-to-action">Login</button>
    *                  <a class="forgot-password" href="/help/">Forgot Password?</a>
    *                </div>
    *              </div>
    *            </form>
    *          </div>
    *        </div>
    *      </div>
    * }}}
    */
  private def verifyAuthentication(newAuthentication: Authentication): Future[Unit] = {
    val uri: Uri = s"${core.Domain}/members/${newAuthentication.session.username}/"
    for {
      page <- getPage(uri)(newAuthentication)
      loginButton = (page filter Tag("div") && Class("login-form-wrapper")).headOption
      _ <- when(loginButton.isDefined) failWith FailedToVerifyNewAuthenticationException(uri)
    } yield ()
  }

  private def get(uri: Uri, headers: Seq[HttpHeader] = Nil): HttpRequest = {
    DefaultSGAuthentication {
      HttpRequest(
        method  = GET,
        uri     = uri,
        headers = headers
      )
    }
  }

  private def post(uri: Uri, headers: Seq[HttpHeader], entity: RequestEntity): HttpRequest = {
    DefaultSGAuthentication {
      HttpRequest(
        method  = POST,
        uri     = uri,
        headers = headers,
        entity  = entity
      )
    }
  }

  implicit class BuffedHttpResponse(val r: HttpResponse)(implicit val ec: ExecutionContext) {

    def entityAsString: Future[String] =
      r.entity.dataBytes.runFold(ByteString(""))(_ ++ _) map (_.decodeString("UTF-8"))
  }

}
