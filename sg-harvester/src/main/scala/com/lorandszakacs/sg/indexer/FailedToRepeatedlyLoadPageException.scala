package com.lorandszakacs.sg.indexer

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 03 Jul 2016
  *
  */
final case class FailedToRepeatedlyLoadPageException(offset: Int, cause: Throwable)
    extends Exception(
      s"Failed while repeatedly opening a page. At offset: $offset. Cause: ${cause.getMessage}",
      cause,
    )
