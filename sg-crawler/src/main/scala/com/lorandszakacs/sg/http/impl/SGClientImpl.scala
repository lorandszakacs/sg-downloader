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
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.lorandszakacs.sg.http.{FailedToGetPageException, SGClient}
import com.lorandszakacs.util.html._

import scala.concurrent.{ExecutionContext, Future}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 03 Jul 2016
  *
  */
private[http] object SGClientImpl {
  private[http] def apply()(implicit actorSystem: ActorSystem, ec: ExecutionContext) = new SGClientImpl(identity)

  def apply(authenticate: HttpRequest => HttpRequest)(implicit actorSystem: ActorSystem, ec: ExecutionContext) = {
    new SGClientImpl(authenticate)
  }

}

private[impl] final class SGClientImpl private(val authenticate: HttpRequest => HttpRequest)(implicit val actorSystem: ActorSystem, val ec: ExecutionContext) extends SGClient {

  private val http: HttpExt = Http()
  private implicit val materializer = ActorMaterializer()

  override def getPage(uri: Uri): Future[Html] = {
    val req = HttpRequest(
      method = GET,
      uri = uri
    )
    val reqWithAuth = authenticate(req)

    for {
      response <- http.singleRequest(reqWithAuth)
      body <- if (response.status == StatusCodes.OK || response.status == StatusCodes.NotModified) {
        response.entity.dataBytes.runFold(ByteString(""))(_ ++ _)
      } else {
        Future.failed(FailedToGetPageException(uri, response))
      }
      html = Html(body.decodeString("UTF-8"))
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
  override def authenticate(username: String, plainTextPassword: String): Future[(HttpRequest => HttpRequest)] = {


    Future.failed(new RuntimeException("... lol, you forgot to delete SGClientImpl.authenticate"))
  }

}