package com.lorandszakacs.sg.http

import org.http4s._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 03 Jul 2016
  *
  */
final case class FailedToGetPageException(uri: Uri)
    extends Exception(
      s"Failed to get page from `${uri.toString}`",
    )

//final case class FailedToGetSGHomepageOnLoginException(uri: Uri, statusCode: Status)
//    extends Exception(
//      s"Failed to GET $uri. As first step of login. Status: $statusCode"
//    )

//final case class ExpectedTwoSetCookieHeadersFromHomepage(uri: Uri, headers: Headers)
//    extends Exception(
//      s"Expected two `Set-Cookie` headers from GET $uri. Instead got following headers: ${stringifyHeaders(headers)}"
//    )

//final case class ExpectedCSRFTokenOnSGHomepageException(uri: Uri)
//    extends Exception(
//      s"Expected a cookie with `csrftoken` from GET $uri. But did not receive one."
//    )
//
//final case class ExpectedSessionIdSGHomepageException(uri: Uri)
//    extends Exception(
//      s"Expected a cookie with `sessionid` from GET $uri. But did not receive one."
//    )

final case class FailedToVerifyNewAuthenticationException(uri: Uri)
    extends Exception(
      s"All login steps were completed successfully, but failed to validate new authentication. GET $uri had a login button when it should not have had one.",
    )

case object NoSessionFoundException
    extends Exception(s"Could not find sesssion in Database. Please fill in manually and try again.")

//final case class FailedToPostLoginException()
//    extends Exception(
//      s"Failed at the second step of the login process."
//    )
//
//final case class ExpectedTwoSetCookieHeadersFromLoginResponseException(uri: Uri, headers: Headers)
//    extends Exception(
//      s"Expected two `Set-Cookie` headers from POST $uri. Instead got following headers: ${stringifyHeaders(headers)}"
//    )
//
//final case class ExpectedCSRFTokenOnSGLoginResponseException(uri: Uri)
//    extends Exception(
//      s"Expected a cookie with `csrftoken` from POST $uri. But did not receive one."
//    )

//final case class ExpectedSessionIdSGLoginResponseException(uri: Uri)
//    extends Exception(
//      s"Expected a cookie with `sessionid` from POST $uri. But did not receive one."
//    )
