package com.lorandszakacs.sg.http

import akka.http.scaladsl.model.{HttpResponse, Uri}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 03 Jul 2016
  *
  */

final case class FailedToGetPageException(uri: Uri, response: HttpResponse) extends Exception(
  s"Failed to get page from `${uri.toString}`. Status: ${response.status}. Entity:\n${response.entity.toString}"
)
