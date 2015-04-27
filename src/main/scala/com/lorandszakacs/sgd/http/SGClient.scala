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

import com.lorandszakacs.sgd.daoservice.api.{PhotoSet, SuicideGirl}
import com.typesafe.scalalogging.StrictLogging

import scala.collection.mutable.ListBuffer
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

import com.lorandszakacs.util.html._

import akka.actor.ActorSystem
import spray.http.Uri
import spray.http.Uri.apply

object SGClient {
  private val initialAccessPoint = "https://suicidegirls.com"
  private val loginAccessPoint   = "https://suicidegirls.com/login/"

  private val referrer = "https://suicidegirls.com/"

  def apply(userName: String, password: String)(implicit actorSystem: ActorSystem, executionContext: ExecutionContext): Try[SGClient] = {
    Login(initialAccessPoint, loginAccessPoint, referrer, userName, password).map(info => new SGClient(info))
  }

  def apply()(implicit actorSystem: ActorSystem, executionContext: ExecutionContext) = {
    new SGClient(NoAuthenticationInfo)
  }
}

class SGClient private(override val authentication: AuthenticationInfo)
  (override implicit val actorSystem: ActorSystem, override implicit val executionContext: ExecutionContext) extends Client with StrictLogging {

  def getSuicideGirl(name: String): Future[Try[SuicideGirl]] = {
    val shallowSets: Future[List[PhotoSet]] = getPhotoSetUris(name).map(_.get).flatMap { photoSetUris: List[Uri] =>
      val listOfFutureSets = photoSetUris map { uri =>
        val e: Future[PhotoSet] = getPhotoSet(photoSetUri(uri)).map(_.get)
        e
      }
      Future.sequence(listOfFutureSets)
    }
    shallowSets map { sets =>
      Success(SuicideGirl(
        uri = photoSetsPageUri(name),
        name,
        photoSets = sets))
    }
  }

  private def photoSetsPageUri(name: String): Uri =
    Uri(s"https://suicidegirls.com/girls/${name.toLowerCase }/photos/view/photosets/")

  private def photoSetUri(suffix: Uri) =
    Uri(s"https://suicidegirls.com${suffix.toString() }")

  def getPhotoSet(albumPageUri: Uri): Future[Try[PhotoSet]] = {
    getPage(albumPageUri) map { html =>
      SGContentParser.parsePhotoSetPage(html, albumPageUri)
    } recover {
      case e: Throwable => Failure(new Exception(s"Failed to get PhotoSet for uri:$albumPageUri, because:`${e.getMessage }`", e))
    }
  }

  def getPhotoSetUris(name: String): Future[Try[List[Uri]]] = {
    def isEndPage(html: Html) = {
      val PartialPageLoadingEndMarker = "No photos available."
      html.document.body().text().take(PartialPageLoadingEndMarker.length).contains(PartialPageLoadingEndMarker)
    }
    val sgPhotoSetsPage = photoSetsPageUri(name)

    loadPageRepeatedly[Uri](sgPhotoSetsPage, 9, SGContentParser.gatherPhotoSetLinks, isEndPage)
  }

  def gatherSGNames(limit: Int): Future[Try[List[String]]] = {
    def isEndPage(html: Html) = {
      val PartialPageLoadingEndMarker = "Sorry, no users match your criteria."
      html.document.body().text().take(PartialPageLoadingEndMarker.length).contains(PartialPageLoadingEndMarker)
    }

    loadPageRepeatedly[String](sgListPageUri, 12, SGContentParser.gatherSGNames, isEndPage, limit)
  }

  private def sgListPageUri: Uri =
    Uri(s"https://suicidegirls.com/profiles/girl/followers/")

  private def loadPageRepeatedly[T](
    uri: Uri, offsetStep: Int,
    parsingFunction: Html => Try[List[T]],
    isEndPage: Html => Boolean,
    cutOffLimit: Int = Int.MaxValue
    ): Future[Try[List[T]]] = {

    def requestMore(uri: Uri, offset: Int) =
      Uri(s"$uri?partial=true&offset=$offset")

    getPage(uri) map { firstHtml =>
      val photoSetUris = ListBuffer[T]()
      photoSetUris ++= parsingFunction(firstHtml).get

      var offset = offsetStep
      var stop = false
      do {
        val newPage = Await.result(getPage(requestMore(uri, offset)), 1 minute)
        offset += offsetStep
        if (isEndPage(newPage) || offset > cutOffLimit) {
          stop = true
        } else {
          //FIXME: add logging
          logger.info(s"\n\tload repeatedly: $offset $offsetStep")
          parsingFunction(newPage) match {
            case Success(s) =>
              photoSetUris ++= s
            case Failure(e) => throw new Exception(s"Failed while gathering the subsequent pages. At offset: $offset", e)
          }
        }
      } while (!stop)
      Success(photoSetUris.toList)
    }
  }

  def gatherHopefulNames(limit: Int): Future[Try[List[String]]] = {
    def isEndPage(html: Html) = {
      val PartialPageLoadingEndMarker = "Sorry, no users match your criteria."
      html.document.body().text().take(PartialPageLoadingEndMarker.length).contains(PartialPageLoadingEndMarker)
    }

    loadPageRepeatedly[String](hopefulListPageUri, 12, SGContentParser.gatherHopefulNames, isEndPage, limit)
  }

  private def hopefulListPageUri: Uri =
    Uri(s"https://suicidegirls.com/profiles/hopeful/followers/")

}