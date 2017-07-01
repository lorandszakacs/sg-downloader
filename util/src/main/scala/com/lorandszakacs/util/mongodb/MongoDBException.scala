package com.lorandszakacs.util.mongodb

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 01 Jul 2017
  *
  */
case class MongoDBException(code: Option[String] = None, msg: Option[String] = None) extends RuntimeException(
  s"MongoDB error: code: $code, msg: $msg"
)
