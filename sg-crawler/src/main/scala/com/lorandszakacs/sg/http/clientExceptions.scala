package com.lorandszakacs.sg.http

import akka.http.scaladsl.model.{HttpHeader, StatusCode, HttpResponse, Uri}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 03 Jul 2016
  *
  */

final case class FailedToGetPageException(uri: Uri, response: HttpResponse) extends Exception(
  s"Failed to get page from `${uri.toString}`. Status: ${response.status}. Entity:\n${response.entity.toString}"
)

final case class FailedToGetSGHomepageOnLoginException(uri: Uri, statusCode: StatusCode) extends Exception(
  s"Failed to GET $uri. As first step of login. Status: $statusCode"
)

final case class ExpectedTwoSetCookieHeadersFromHomepage(uri: Uri, headers: Seq[HttpHeader]) extends Exception(
  s"Expected two `Set-Cookie` headers from GET $uri. Instead got following headers: ${headers.map(h => s"${h.name()} : ${h.value()}").mkString(";;")}"
)
