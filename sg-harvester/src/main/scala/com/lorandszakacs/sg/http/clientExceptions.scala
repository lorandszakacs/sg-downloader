package com.lorandszakacs.sg.http

import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.util.ByteString

import scala.concurrent.{ExecutionContext, Future, Await}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Try

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 03 Jul 2016
  *
  */
private[http] object ExceptionHelpers {
  def stringifyHeaders(hs: Seq[HttpHeader]) = {
    hs.map(h => s"${h.name()} : ${h.value()}").mkString("{", "\n", "}")
  }

  def stringifyEntity(e: ResponseEntity)(implicit mat: ActorMaterializer, ec: ExecutionContext): Option[String] = {
    val f: Future[String] = e.dataBytes.runFold(ByteString(""))(_ ++ _) map (_.decodeString("UTF-8"))
    Try(Await.result(f, 1 minute)).toOption
  }

  def stringifyEntity(e: RequestEntity)(implicit mat: ActorMaterializer, ec: ExecutionContext): Option[String] = {
    val f: Future[String] = e.dataBytes.runFold(ByteString(""))(_ ++ _) map (_.decodeString("UTF-8"))
    Try(Await.result(f, 1 minute)).toOption
  }

  implicit class BuffedResponse(response: HttpResponse)(implicit mat: ActorMaterializer, ec: ExecutionContext) {
    def stringify: String =
      s"Status: ${response._1}. \nHeaders:\n${stringifyHeaders(response.headers)}.\nEntity:\n${if (response.status != StatusCodes.NotFound) stringifyEntity(response._3).getOrElse("None") else "None"}"
  }

  implicit class BuffedRequest(req: HttpRequest)(implicit mat: ActorMaterializer, ec: ExecutionContext) {
    def stringify: String =
      s"Uri: ${req.uri}\nMethod: ${req.method}\nHeaders:\n${stringifyHeaders(req.headers)}.\nEntity:\n${stringifyEntity(req._4).getOrElse("None")}"
  }

}

import ExceptionHelpers._

final case class FailedToGetPageException(uri: Uri, req: HttpRequest, response: HttpResponse)(implicit mat: ActorMaterializer, ec: ExecutionContext) extends Exception(
  s"Failed to get page from `${uri.toString}`. Response: ${response.stringify}\nRequest Headers:\n${stringifyHeaders(req.headers)}"
)

final case class FailedToGetSGHomepageOnLoginException(uri: Uri, statusCode: StatusCode) extends Exception(
  s"Failed to GET $uri. As first step of login. Status: $statusCode"
)

final case class ExpectedTwoSetCookieHeadersFromHomepage(uri: Uri, headers: Seq[HttpHeader]) extends Exception(
  s"Expected two `Set-Cookie` headers from GET $uri. Instead got following headers: ${stringifyHeaders(headers)}"
)

final case class ExpectedCSRFTokenOnSGHomepageException(uri: Uri) extends Exception(
  s"Expected a cookie with `csrftoken` from GET $uri. But did not receive one."
)

final case class ExpectedSessionIdSGHomepageException(uri: Uri) extends Exception(
  s"Expected a cookie with `sessionid` from GET $uri. But did not receive one."
)


final case class FailedToVerifyNewAuthenticationException(uri: Uri) extends Exception(
  s"All login steps were completed successfully, but failed to validate new authentication. GET $uri had a login button when it should not have had one."
)

final case class FailedToPostLoginException(request: HttpRequest, response: HttpResponse)(implicit mat: ActorMaterializer, ec: ExecutionContext) extends Exception(
  s"Failed at the second step of the login process. Request:\n${request.stringify}Response:\n${response.stringify}\n\n"
)

final case class ExpectedTwoSetCookieHeadersFromLoginResponseException(uri: Uri, headers: Seq[HttpHeader]) extends Exception(
  s"Expected two `Set-Cookie` headers from POST $uri. Instead got following headers: ${stringifyHeaders(headers)}"
)

final case class ExpectedCSRFTokenOnSGLoginResponseException(uri: Uri) extends Exception(
  s"Expected a cookie with `csrftoken` from POST $uri. But did not receive one."
)

final case class ExpectedSessionIdSGLoginResponseException(uri: Uri) extends Exception(
  s"Expected a cookie with `sessionid` from POST $uri. But did not receive one."
)