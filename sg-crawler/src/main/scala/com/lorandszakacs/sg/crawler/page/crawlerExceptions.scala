package com.lorandszakacs.sg.crawler.page

import com.lorandszakacs.util.html.Html

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 03 Jul 2016
  *
  */
final case class FailedToRepeatedlyLoadPageException(offset: Int, cause: Throwable) extends Exception(
  s"Failed while repeatedly opening a page. At offset: $offset. Cause: ${cause.getMessage}",
  cause
)

final case class HTMLPageDidNotContainAnyPhotoSetLinksException(html: Html) extends Exception(
  s"""
     |HTML page did not contain any photo set links:
     |${html.toString}
  """.stripMargin
)

