package com.lorandszakacs.sg

import java.net.URL

import org.http4s.Uri

import scala.language.implicitConversions

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 11 Jul 2016
  *
  */
trait URLConversions {

  implicit def http4sURIToJavaURL(uri: Uri): URL = new URL(uri.toString)

  implicit def javaURLtoTttp4sUri(url: URL): Uri = Uri.unsafeFromString(url.toExternalForm)

  implicit def stringToURL(str: String): URL = new URL(str)

  implicit def stringToUri(str: String): Uri = Uri.unsafeFromString(str)

}
