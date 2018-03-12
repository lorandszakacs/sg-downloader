package com.lorandszakacs.util.mongodb

import reactivemongo.api
import reactivemongo.bson.DefaultBSONHandlers

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 26 Jun 2017
  *
  */
object MongoDBTypes extends MongoDBTypes

trait MongoDBTypes extends MongoDBBSONTypes with DefaultBSONHandlers {
  type Cursor[T] = reactivemongo.api.Cursor[T]
  val Cursor: api.Cursor.type = reactivemongo.api.Cursor

}
