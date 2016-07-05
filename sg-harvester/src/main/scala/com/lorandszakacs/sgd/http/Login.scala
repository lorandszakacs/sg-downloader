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

import scala.concurrent._
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.Try

import akka.actor.ActorSystem
import akka.http.scaladsl._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.HttpMethods._

//object Login {
//  def apply(initialAccessPoint: Uri, loginAccessPoint: Uri, referer: String, user: String, pwd: String)
//    (implicit actorSystem: ActorSystem, executionContext: ExecutionContext): Try[AuthenticationInfo] = {
//    val loginFuture = for {
//      loginPage: HttpResponse <- Http().singleRequest(HttpRequest(GET, initialAccessPoint))
//
//      loginInfo: LoginInfo = LoginInfo(loginPage.headers, referer).getOrElse(throw new Exception("Response headers did not contain the CSRF token"))
//
//      loginRequest: HttpRequest = loginInfo(Post(loginAccessPoint), user, pwd)
//
//      loginResponse: HttpResponse <- loginRequest ~> sendReceive
//
//      _ <- Future {
//        if (!(loginResponse.status == StatusCodes.OK || loginResponse.status == StatusCodes.Created))
//          throw new Exception(s"Login failed. The returned status code was: ${loginResponse.status}. The entire response was:\n${loginResponse.toString}")
//      }
//
//      authenticationInfo = AuthenticationInfo(loginResponse.headers, referer)
//
//    } yield authenticationInfo match {
//      case None => throw new Exception(s"Login failed or something changed on the server side. The login attempt returned status: ${loginResponse.status}. But the appropriate header tokens were not found or were malformed.")
//      case Some(info) =>
//        info
//    }
//
//    Try(Await.result(loginFuture, 1 minute))
//  }
//
//}
//
//sealed trait CSRFInfo {
//  def referer: String
//
//  def csrfTokenCookie: HttpCookie
//
//  def cookieHeader: HttpHeader
//
//  /**
//    * This method might be obsolete. The browser includes this in the header,
//    * but login works without this header as well.
//    */
//  def XCSRFTokenHeader: HttpHeader = RawHeader("X-CSRFToken", csrfTokenCookie.content)
//
//  def gaCookie: HttpCookie = HttpCookie("_ga", "GA1.2.1718702671.1408266088")
//
//  //TODO: move somewhere else
//  def refererHeader: HttpHeader = RawHeader("Referer", referer)
//
//  def apply(originalRequest: HttpRequest): HttpRequest = originalRequest ~> cookieHeader ~> XCSRFTokenHeader ~> refererHeader
//
//  def and(originalRequest: HttpRequest): HttpRequest = this.apply(originalRequest)
//
//  override def toString = s"\n${getClass.getSimpleName}\n--headers:\n\t${cookieHeader.toString}\n\t${XCSRFTokenHeader.toString}\n\t${refererHeader.toString}"
//}
//
//object CookieHeaderInterpreter {
//  def extractCookie(headers: List[HttpHeader], cookieName: String): Option[HttpCookie] = {
//    val cookieHeadersOnly = headers.filter(c => (c.name == "Set-Cookie") && c.value.contains(cookieName) && c.value.contains("="))
//    val cookieHeaders = cookieHeadersOnly map { c =>
//      val nothingBefore = c.value.drop(c.value.indexOf(cookieName))
//      val nothingAfter = if (nothingBefore.contains(";")) nothingBefore.take(nothingBefore.indexOf(";")) else nothingBefore
//      val split = nothingAfter.split("=")
//      HttpCookie(split(0), split(1))
//    }
//    cookieHeaders.find(_.name == cookieName)
//  }
//}
//
//private object LoginInfo {
//  final val CSRFCookieName = "csrftoken"
//
//  def apply(headers: Seq[HttpHeader], referer: String): Option[LoginInfo] = {
//    CookieHeaderInterpreter.extractCookie(headers, CSRFCookieName) map { csrfCookie =>
//      new LoginInfo(csrfCookie, referer)
//    }
//  }
//}
//
//private class LoginInfo private(val csrfTokenCookie: HttpCookie, val referer: String) extends CSRFInfo {
//  def cookieHeader = Cookie(csrfTokenCookie, gaCookie)
//
//  def formData(user: String, pwd: String) = FormData(Seq(
//    ("csrfmiddlewaretoken", csrfTokenCookie.content),
//    "username" -> user,
//    "password" -> pwd))
//
//  def apply(originalRequest: HttpRequest, user: String, pwd: String): HttpRequest = {
//    super.apply(Post(originalRequest.uri, formData(user, pwd)))
//  }
//
//}
//
//private object AuthenticationInfo {
//  final val CSRFCookieName = "csrftoken"
//  final val SessionIdCookieName = "sessionid"
//
//  def apply(headers: List[HttpHeader], referer: String): Option[AuthenticationInfo] = {
//    for {
//      csrfCookie <- CookieHeaderInterpreter.extractCookie(headers, CSRFCookieName)
//      sessionIdCookie <- CookieHeaderInterpreter.extractCookie(headers, SessionIdCookieName)
//    } yield new AuthenticationInfo(csrfCookie, sessionIdCookie, referer)
//  }
//}
//
//class AuthenticationInfo(val csrfTokenCookie: HttpCookie, val sessionIdCookie: HttpCookie, val referer: String) extends CSRFInfo {
//  def cookieHeader = Cookie(sessionIdCookie, csrfTokenCookie, gaCookie)
//}
//
//object NoAuthenticationInfo extends AuthenticationInfo(null, null, null) {
//  override def apply(originalRequest: HttpRequest): HttpRequest = originalRequest
//
//  override def toString = s"${getClass.getName()}"
//}