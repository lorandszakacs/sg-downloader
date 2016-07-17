package com.lorandszakacs.sg.exporter.indexwriter

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 17 Jul 2016
  *
  */
final case class RootFolderCouldNotBeOpenedException(folderPath: String, cause: Throwable) extends Exception(
  s"Root folder: $folderPath could not be cleaned because: ${cause.getMessage}",
  cause
)

final case class RootFolderFileCouldNotBeDeleted(fp: String, cause: Throwable) extends Exception(
  s"file: $fp could not be deleted because: ${cause.getMessage}",
  cause
)

final case class FailedToCreateFolderException(fp: String) extends Exception(
  s"failed to create folder: $fp."
)