package com.lorandszakacs.sg.http

import akka.http.scaladsl.model._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 03 Jul 2016
  *
  */

final case class FailedToGetPageException(uri: Uri, req: HttpRequest, response: HttpResponse) extends Exception(
  s"Failed to get page from `${uri.toString}`. Status: ${response.status}. Entity:\n${response.entity.toString}. Request Headers:\n${req.headers.map(h => s"${h.name()} :  ${h.value()}").mkString("\n")}"
)

final case class FailedToGetSGHomepageOnLoginException(uri: Uri, statusCode: StatusCode) extends Exception(
  s"Failed to GET $uri. As first step of login. Status: $statusCode"
)

final case class ExpectedTwoSetCookieHeadersFromHomepage(uri: Uri, headers: Seq[HttpHeader]) extends Exception(
  s"Expected two `Set-Cookie` headers from GET $uri. Instead got following headers: ${headers.map(h => s"${h.name()} : ${h.value()}").mkString(";;")}"
)

final case class ExpectedCSRFTokenOnSGHomepageException(uri: Uri) extends Exception(
  s"Expected a cookie with `csrftoken` from $uri. But did not receive one."
)

final case class ExpectedSessionIdSGHomepageException(uri: Uri) extends Exception(
  s"Expected a cookie with `sessionid` from $uri. But did not receive one."
)


final case class FailedToVerifyNewAuthenticationException(uri: Uri) extends Exception(
  s"All login steps were completed successfully, but failed to validate new authentication. GET $uri had a login button when it should not have had one."
)