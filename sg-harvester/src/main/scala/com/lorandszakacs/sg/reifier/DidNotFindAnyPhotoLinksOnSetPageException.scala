package com.lorandszakacs.sg.reifier

import akka.http.scaladsl.model.Uri

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 02 Jul 2017
  *
  */
final case class DidNotFindAnyPhotoLinksOnSetPageException(pageURL: Uri)
    extends Exception(
      s"Did not find any photos on page: $pageURL"
    )
