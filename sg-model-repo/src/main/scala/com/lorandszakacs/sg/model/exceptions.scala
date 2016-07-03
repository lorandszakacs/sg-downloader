package com.lorandszakacs.sg.model

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 03 Jul 2016
  *
  */
final case class PhotoSetAlreadyExistsException(sgName: String, photoSet: PhotoSet) extends Exception(
  s"Photoset `${photoSet.title}` of SG: `$sgName` already exists."
)

final case class PhotoSetDoesNotExistException(sgName: String, photoSet: PhotoSet) extends Exception(
  s"Photoset `${photoSet.title}` of SG: `$sgName` does not exist."
)