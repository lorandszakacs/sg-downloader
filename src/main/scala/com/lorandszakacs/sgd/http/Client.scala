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
package com.lorandszakacs.sgd.http

import akka.actor.ActorSystem
import com.lorandszakacs.util.html._
import spray.client.pipelining._
import spray.http.{StatusCodes, Uri}

import scala.concurrent.{ExecutionContext, Future}

/**
 * @author Lorand Szakacs, lsz@lorandszakacs.com
 * @since 16 Mar 2015
 *
 */
trait Client {

  private val authenticate = authentication

  implicit def actorSystem: ActorSystem

  implicit def executionContext: ExecutionContext

  def authentication: AuthenticationInfo

  def getPage(uri: Uri): Future[Html] = {
    akka.http.model.HttpRequest
    (authenticate and Get(uri)) ~> sendReceive map { response =>
      if (response.status == StatusCodes.OK || response.status == StatusCodes.NotModified) {
        Html(response.entity.asString)
      } else {
        throw new Exception(s"Failed to get page:${uri.toString}.\nResponse: ${response.toString}")
      }
    }
  }


}