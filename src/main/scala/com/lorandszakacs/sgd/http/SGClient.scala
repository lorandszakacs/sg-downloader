/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Lorand Szakacs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.lorandszakacs.sgd.http

import java.time.LocalDate

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

import com.lorandszakacs.commons.html._
import com.lorandszakacs.sgd.model._

import akka.actor.ActorSystem
import spray.http.Uri.apply

object SGClient {
  private val initialAccessPoint = "https://suicidegirls.com"
  private val loginAccessPoint = "https://suicidegirls.com/login/"

  private val referer = "https://suicidegirls.com/"

  def apply(userName: String, password: String)(implicit actorSystem: ActorSystem, executionContext: ExecutionContext): Try[SGClient] = {
    Login(initialAccessPoint, loginAccessPoint, referer, userName, password).map(info => new SGClient(info))
  }
}

class SGClient private (val authentication: AuthenticationInfo)(implicit val actorSystem: ActorSystem, val executionContext: ExecutionContext) extends Client {

  private def albumPageUri(name: String) = s"https://suicidegirls.com/girls/${name}/photos/view/photosets/"

  def getPhotos(albumPage: Html): List[PhotoShallow] = {
    albumPage filter Class("image-section") && Tag("li") && Class("photo-container") && RetainFirst(HrefLink()) match {
      case Some(links) => links.zip(1 to links.length).map(pair => PhotoShallow(pair._1, pair._2))
      case None => throw new Exception(s"Failed to extract any Photo from this document:${albumPage.document.toString}")
    }
  }

}