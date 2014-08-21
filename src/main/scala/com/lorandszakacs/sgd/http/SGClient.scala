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

import scala.collection.mutable.ListBuffer
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

import com.lorandszakacs.commons.html._
import com.lorandszakacs.sgd.model._

import akka.actor.ActorSystem
import spray.http.Uri
import spray.http.Uri.apply

object SGClient {
  private val initialAccessPoint = "https://suicidegirls.com"
  private val loginAccessPoint = "https://suicidegirls.com/login/"

  private val referer = "https://suicidegirls.com/"

  def apply(userName: String, password: String)(implicit actorSystem: ActorSystem, executionContext: ExecutionContext): Try[SGClient] = {
    Login(initialAccessPoint, loginAccessPoint, referer, userName, password).map(info => new SGClient(info))
  }

  def apply()(implicit actorSystem: ActorSystem, executionContext: ExecutionContext) = {
    new SGClient(new NoAuthenticationInfo)
  }
}

class SGClient protected (val authentication: AuthenticationInfo)(implicit val actorSystem: ActorSystem, val executionContext: ExecutionContext) extends Client {

  private def photoSetsPageUri(name: String) = s"https://suicidegirls.com/girls/${name.toLowerCase}/photos/view/photosets/"

  def getPhotoSet(albumPageUri: Uri): Future[Try[PhotoSetShallow]] = {
    getPage(albumPageUri) map { html =>
      Parser.parsePhotoSetPage(html, albumPageUri)
    } recover {
      case e: Throwable => Failure(new Exception(s"Failed to get PhotoSet for uri:${albumPageUri}, because:`${e.getMessage()}`", e))
    }
  }

  def getPhotoSetUris(name: String): Future[Try[List[Uri]]] = {
    val sgPhotoSetsPage = photoSetsPageUri(name)

    def isEndPage(html: Html) = {
      val PartialPageLoadingEndMarker = "No photos available."
      html.document.body().text().take(PartialPageLoadingEndMarker.length).contains(PartialPageLoadingEndMarker)
    }
    def requestMore(uri: Uri, offset: Int) = Uri(s"${uri.toString}?partial=true&offset=${offset}")

    def loadPageRepeatedly(firstHtml: Html): Try[List[Uri]] = {
      val photoSetUris = ListBuffer[Uri]()
      photoSetUris ++= Parser.gatherPhotoSetLinks(firstHtml).get

      val offsetStep = 9
      var offset = offsetStep
      println("initialSize=" + photoSetUris.length)
      var stop = false
      do {
        println(s"New page: ${offset}")
        val newPage = Await.result(getPage(requestMore(sgPhotoSetsPage, offset)), 1 minute)
        offset += offsetStep
        if (isEndPage(newPage) || offset > 100) {
          stop = true
        } else {
          Parser.gatherPhotoSetLinks(newPage) match {
            case Success(s) =>
              photoSetUris ++= s
              println("New size=" + photoSetUris.size)
            case Failure(e) => throw new Exception(s"Failed while gathering the subsequent pages. At offset: ${offset}", e)
          }
        }
      } while (!stop)
      Success(photoSetUris.toList)
    }

    getPage(sgPhotoSetsPage) map { html =>
      loadPageRepeatedly(html)
    } recover {
      case e: Throwable => Failure(e)
    }
  }

}