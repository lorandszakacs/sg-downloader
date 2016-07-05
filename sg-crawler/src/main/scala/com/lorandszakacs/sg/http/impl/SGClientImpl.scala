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

import akka.actor.ActorSystem
import akka.http.scaladsl._
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.lorandszakacs.sg.http._
import com.lorandszakacs.util.html._

import scala.collection.immutable.Seq
import scala.concurrent.{ExecutionContext, Future}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 03 Jul 2016
  *
  */
private[http] object SGClientImpl {
  private[http] def apply()(implicit actorSystem: ActorSystem, ec: ExecutionContext) = new SGClientImpl()
}

private[impl] final class SGClientImpl private()(implicit val actorSystem: ActorSystem, val ec: ExecutionContext) extends SGClient {

  private val http: HttpExt = Http()
  private implicit val materializer = ActorMaterializer()

  override def getPage(uri: Uri)(implicit authentication: Authentication): Future[Html] = {
    val req = HttpRequest(
      method = GET,
      uri = uri
    )
    val reqWithAuth = authentication(req)

    for {
      response <- http.singleRequest(reqWithAuth)
      body <- if (response.status == StatusCodes.OK || response.status == StatusCodes.NotModified) {
        response.entityAsString
      } else {
        Future.failed(FailedToGetPageException(uri, response))
      }
      html = Html(body)
    } yield html
  }

  /**
    * Steps:
    * ------------------------------------------
    * 1. GET ``https://www.suicidegirls.com/``
    * we add clientSide the cookies:
    * {{{
    *   Origin: https://www.suicidegirls.com
    *   Cookie: sessionid="gAJ9cQEoVQpnZW5lcmljX2FkcQJOVQJhZHEDTnUu:1bK62V:sirfAXi5Ay75rzDpJwB5tGhZH0Q"; csrftoken=ntk89cZcgo7hynvSMpDMdYxW75hIjo1Z
    * }}}
    *
    * we receive:
    *
    * {{{
    *   Set-Cookie: csrftoken=ntk89cZcgo7hynvSMpDMdYxW75hIjo1Z; expires=Mon, 03-Jul-2017 15:44:51 GMT; Max-Age=31449600; Path=/
    *   Set-Cookie: sessionid="gAJ9cQEoVQJhZE5VCmdlbmVyaWNfYWROdS4:1bK63H:i1FhbU7q9BwYx5pXybGPe4V32u0"; expires=Mon, 18-Jul-2016 15:44:51 GMT; httponly; Max-Age=1209600; Path=/
    * }}}
    * let ``CSRF``=ntk89cZcgo7hynvSMpDMdYxW75hIjo1Z
    * let ``SessionID``="gAJ9cQEoVQJhZE5VCmdlbmVyaWNfYWROdS4:1bK63H:i1FhbU7q9BwYx5pXybGPe4V32u0"
    *
    * ------------------------------------------
    * 2. POST ``https://www.suicidegirls.com/login``
    * we add headers:
    * {{{
    *   Origin: https://www.suicidegirls.com
    *   Cookie: csrftoken=``$CSRF``; sessionid=``$SessionID``
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
    *   Referer: https://www.suicidegirls.com/
    *   Cookie: sessionid=``$FinalSessionID``; csrftoken=``$FinalCSRFToken``
    * }}}
    */
  def authenticate(username: String, plainTextPassword: String): Future[Authentication] = {
    case class InitialData(
      csrfToken: String,
      SessionID: String
    )
    def getCookiesFromStartpage: Future[InitialData] = {
      //TODO: generate random values for these two cookies
      val cookies = Cookie(
        HttpCookiePair("sessionid", "gAJ9cQEoVQpnZW5lcmljX2FkcQJOVQJhZHEDTnUu:1bK62V:sirfAXi5Ay75rzDpJwB5tGhZH0Q"),
        HttpCookiePair("csrftoken", "ntk89cZcgo7hynvSMpDMdYxW75hIjo1Z")
      )
      val headers: List[HttpHeader] = List(
        cookies,
        Origin(HttpOrigin("https://www.suicidegirls.com"))
      )
      val getRequest = HttpRequest(
        method = GET,
        uri = "https://www.suicidegirls.com/",
        headers = headers
      )
      for {
        response <- http.singleRequest(getRequest)
        cookies <- if (response.status != StatusCodes.OK) {
          Future.failed(FailedToGetSGHomepageOnLoginException(getRequest.uri, response.status))
        } else {
          val headers = response._2
          val cookies: Seq[HttpCookie] = headers.filter(_.is("set-cookie")).map(c => HttpCookiePair(c.name(), c.value()).toCookie())
          if (cookies.length != 2) {
            Future.failed(ExpectedTwoSetCookieHeadersFromHomepage(getRequest.uri, headers))
          } else {
            println {
              s"""
                 |
                 |------------------------
                 |${cookies.mkString("\n\n")}
                 |------------------------
                 |
              """.stripMargin
            }
            Future.successful(cookies)
          }
        }
      } yield InitialData(
        csrfToken = cookies.head.value,
        SessionID = cookies(1).value
      )
    }

    for {
      initialData <- getCookiesFromStartpage
    } yield { http: HttpRequest =>
      http.cookies
    }

    Future.failed(new RuntimeException("... lol, you forgot to delete SGClientImpl.authenticate"))
  }

  implicit class BuffedHttpResponse(val r: HttpResponse)(implicit val ec: ExecutionContext) {
    def entityAsString: Future[String] = r.entity.dataBytes.runFold(ByteString(""))(_ ++ _) map (_.decodeString("UTF-8"))
  }

}