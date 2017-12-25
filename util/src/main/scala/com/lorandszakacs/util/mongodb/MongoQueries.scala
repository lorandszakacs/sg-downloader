package com.lorandszakacs.util.mongodb

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Jun 2017
  *
  */
object MongoQueries extends MongoQueries

trait MongoQueries {
  val _id: String = "_id"

  val $in:   String = "$in"
  val $size: String = "$size"

  val $gte: String = "$gte"
  val $lte: String = "$lte"
}
