package com.lorandszakacs.util.mongodb

import reactivemongo.api

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Jun 2017
  *
  */
object MongoDBTypes extends MongoDBTypes

trait MongoDBTypes extends MongoDBBSONTypes {
  type Cursor[T] = reactivemongo.api.Cursor[T]
  val Cursor: api.Cursor.type = reactivemongo.api.Cursor


}
