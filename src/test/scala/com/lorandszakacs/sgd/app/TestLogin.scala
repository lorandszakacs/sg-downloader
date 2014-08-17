package com.lorandszakacs.sgd.app

import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import akka.actor.ActorSystem
import spray.client.pipelining._
import spray.http.FormData
import spray.http.HttpCookie
import spray.http.HttpHeader
import spray.http.HttpHeaders.Cookie
import spray.http.HttpHeaders.RawHeader
import spray.http.HttpRequest
import spray.http.HttpResponse
import spray.http.StatusCodes
import scala.io.StdIn

object TestLogin extends App {
  implicit val system = ActorSystem("test-login-client")
  import system.dispatcher

  val Referer = "https://suicidegirls.com/"

  def loginPath(user: String, pwd: String): Future[HttpResponse] = {
    Get("https://suicidegirls.com") ~>
      sendReceive flatMap { getResponse =>
        val loginInfo = LoginInfo(getResponse.headers).getOrElse(throw new Exception("Response headers did not contain the CSRF token"))
        val postRequest = loginInfo(Post("https://suicidegirls.com/login/"), user, pwd)
        println(s"${loginInfo.toString}")
        postRequest ~> sendReceive
      }
  }

  val loginResult = {
    print("username: ")
    val user = StdIn.readLine()
    print("\npassword: ")
    val pwd = StdIn.readLine()
    println()
    loginPath(user, pwd)
  }
  loginResult onComplete {
    case Success(response) =>
      if (response.status == StatusCodes.OK || response.status == StatusCodes.Created) {
        val authenticationInfo = AuthenticationInfo(response.headers)
        authenticationInfo match {
          case None => throw new Exception(s"Login failed or something changed on the server side. The login attempt returned status: ${response.status}. But the appropriate header tokens were not found or were malformed.")
          case Some(info) =>
            println(s"${info.toString}")
            shutdown()
        }
      } else {
        throw new Exception(s"Login failed. The returned status code was: ${response.status}. The entire response was:\n${response.toString}")
      }
    case Failure(e) =>
      println(s"Failed because:\n ${e.getMessage}")
      shutdown()
  }

  def shutdown(): Unit = {
    system.shutdown()
  }

  object CookieHeaderInterpretor {
    def extractCookie(headers: List[HttpHeader], cookieName: String): Option[HttpCookie] = {
      val cookieHeadersOnly = headers.filter(c => (c.name == "Set-Cookie") && c.value.contains(cookieName) && c.value.contains("="))
      val cookieHeaders = cookieHeadersOnly map { c =>
        val nothingBefore = c.value.drop(c.value.indexOf(cookieName))
        val nothingAfter = if (nothingBefore.contains(";")) nothingBefore.take(nothingBefore.indexOf(";")) else nothingBefore
        val split = nothingAfter.split("=")
        HttpCookie(split(0), split(1))
      }
      cookieHeaders.find(_.name == cookieName)
    }
  }

  trait CSRFInfo {
    def csrfTokenCookie: HttpCookie

    def cookieHeader: HttpHeader
    /**
     * This method might be obsolute. The browser includes this in the header,
     * but login works without this header as well.
     */
    def XCSRFTokenHeader: HttpHeader = RawHeader("X-CSRFToken", csrfTokenCookie.content)

    def gaCookie: HttpCookie = HttpCookie("_ga", "GA1.2.1718702671.1408266088")

    //TODO: move somewhere else
    def refererHeader: HttpHeader = RawHeader("Referer", Referer)

    def apply(originalRequest: HttpRequest): HttpRequest = originalRequest ~> cookieHeader ~> XCSRFTokenHeader ~> refererHeader

    override def toString = s"\n${getClass.getSimpleName}\n--headers:\n\t${cookieHeader.toString}\n\t${XCSRFTokenHeader.toString}\n\t${refererHeader.toString}"
  }

  object LoginInfo {
    final val CSRFCookieName = "csrftoken"

    def apply(headers: List[HttpHeader]): Option[LoginInfo] = {
      CookieHeaderInterpretor.extractCookie(headers, CSRFCookieName) map { csrfCookie =>
        new LoginInfo(csrfCookie)
      }
    }
  }

  class LoginInfo private (val csrfTokenCookie: HttpCookie) extends CSRFInfo {
    def cookieHeader = Cookie(csrfTokenCookie, gaCookie)
    def formData(user: String, pwd: String) = FormData(Seq(
      ("csrfmiddlewaretoken", csrfTokenCookie.content),
      "username" -> user,
      "password" -> pwd))

    def apply(originalRequest: HttpRequest, user: String, pwd: String): HttpRequest = {
      super.apply(Post(originalRequest.uri, formData(user, pwd)))
    }

  }

  object AuthenticationInfo {
    final val CSRFCookieName = "csrftoken"
    final val SessionIdCookieName = "sessionid"

    def apply(headers: List[HttpHeader]): Option[AuthenticationInfo] = {
      for {
        csrfCookie <- CookieHeaderInterpretor.extractCookie(headers, CSRFCookieName)
        sessionIdCookie <- CookieHeaderInterpretor.extractCookie(headers, SessionIdCookieName)
      } yield new AuthenticationInfo(csrfCookie, sessionIdCookie)
    }
  }

  class AuthenticationInfo(val csrfTokenCookie: HttpCookie, val sessionIdCookie: HttpCookie) extends CSRFInfo {
    def cookieHeader = Cookie(sessionIdCookie, csrfTokenCookie, gaCookie)
  }

  def sampleLoginResponse =
    """
Remote Address:54.225.132.171:443
Request URL:https://suicidegirls.com/login/
Request Method:POST
Status Code:201 CREATED
Request Headersview source
Accept:*/*
Accept-Encoding:gzip,deflate,sdch
Accept-Language:en-US,en;q=0.8,ro;q=0.6
Connection:keep-alive
Content-Length:89
Content-Type:application/x-www-form-urlencoded; charset=UTF-8
Cookie:csrftoken=RShI5GUhjY5OWlcK3G2UdCCs7QdI1Ukm; _ga=GA1.2.1718702671.1408266088
Host:suicidegirls.com
Origin:https://suicidegirls.com
Referer:https://suicidegirls.com/
User-Agent:Mozilla/5.0 (X11; Linux i686 (x86_64)) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.143 Safari/537.36
X-CSRFToken:RShI5GUhjY5OWlcK3G2UdCCs7QdI1Ukm
X-Requested-With:XMLHttpRequest
Form Dataview sourceview URL encoded
csrfmiddlewaretoken:RShI5GUhjY5OWlcK3G2UdCCs7QdI1Ukm
username:XXXX
password:XXXX
Response Headersview source
Connection:keep-alive
Content-Length:12
Content-Type:application/json
Date:Sun, 17 Aug 2014 11:26:57 GMT
Server:nginx
Set-Cookie:sessionid=".eJxrYKotZNQI5Y1PLC3JiC8tTi2Kz0wpZPIKE-ZgCBVCEk1KTM5OzUspZA7VLk4HCeuB5aDCxXrOpcUl-bmhQKW--SmpOU5Q5SylegCkryM1:1XIybx:8oJGY2l5HIGgt9lMOp2HVtXWmwo"; expires=Sun, 31-Aug-2014 11:26:57 GMT; httponly; Max-Age=1209600; Path=/
Set-Cookie:csrftoken=NCTU9V2gChkrwzJWxWX5F6OKhwNc2NgX; expires=Sun, 16-Aug-2015 11:26:57 GMT; Max-Age=31449600; Path=/
Vary:Cookie, Host
X-Frame-Options:SAMEORIGIN
"""

  def sampleRequestAlbumInfo =
    """
Remote Address:54.243.34.64:443
Request URL:https://suicidegirls.com/api/get_album_info/1462117/
Request Method:GET
Status Code:200 OK
Request Headersview source
----------------------------
Accept:*/*
Accept-Encoding:gzip,deflate,sdch
Accept-Language:en-US,en;q=0.8,ro;q=0.6
Connection:keep-alive
Cookie:sessionid=".eJxrYKotZNQI5Y1PLC3JiC8tTi2Kz0wpZPIKE-ZgCBVCEk1KTM5OzUspZA7VLk4HCeuB5aDCxXrOpcUl-bmhQKW--SmpOU5Q5SylegCkryM1:1XIybx:8oJGY2l5HIGgt9lMOp2HVtXWmwo"; csrftoken=NCTU9V2gChkrwzJWxWX5F6OKhwNc2NgX; _ga=GA1.2.1718702671.1408266088
Host:suicidegirls.com
Referer:https://suicidegirls.com/girls/bixton/album/1462117/chiaroscuro/
User-Agent:Mozilla/5.0 (X11; Linux i686 (x86_64)) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.143 Safari/537.36
X-Requested-With:XMLHttpRequest
-->
Response Headersview source
Allow:GET, OPTIONS
Connection:keep-alive
Content-Encoding:gzip
Content-Length:17480
Content-Type:application/json
Date:Sun, 17 Aug 2014 12:58:26 GMT
Server:nginx
Vary:Accept, Cookie, Host, Accept-Encoding
X-Frame-Options:SAMEORIGIN

"""
}