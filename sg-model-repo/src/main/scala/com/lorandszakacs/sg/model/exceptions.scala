package com.lorandszakacs.sg.model

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 03 Jul 2016
  *
  */
final case class PhotoSetAlreadyExistsException(sgName: ModelName, photoSet: PhotoSet) extends Exception(
  s"Photoset `${photoSet.title}` of SG: `$sgName` already exists."
)

final case class PhotoSetDoesNotExistException(sgName: ModelName, photoSet: PhotoSet) extends Exception(
  s"Photoset `${photoSet.title}` of SG: `$sgName` does not exist."
)