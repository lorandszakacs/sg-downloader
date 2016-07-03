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

private[impl] final class SGClientImpl private(val authenticate: HttpRequest => HttpRequest)(implicit val actorSystem: ActorSystem, val ec: ExecutionContext) extends SGClientImpl {

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
}