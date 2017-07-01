package com.lorandszakacs.util.mongodb

import reactivemongo.{api, bson}
import reactivemongo.bson.Macros

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Jun 2017
  *
  */
object MongoDBTypes extends MongoDBTypes

trait MongoDBTypes {
  type Database = reactivemongo.api.DB
  type Cursor[T] = reactivemongo.api.Cursor[T]

  val Cursor: api.Cursor.type = reactivemongo.api.Cursor
  type BSONCollection = reactivemongo.api.collections.bson.BSONCollection

  type BSONValue = reactivemongo.bson.BSONValue
  type BSONDocument = reactivemongo.bson.BSONDocument
  val BSONDocument: bson.BSONDocument.type = reactivemongo.bson.BSONDocument

  type BSONString = reactivemongo.bson.BSONString
  val BSONString: bson.BSONString.type = reactivemongo.bson.BSONString

  type BSONInteger = reactivemongo.bson.BSONInteger
  val BSONInteger: bson.BSONInteger.type = reactivemongo.bson.BSONInteger

  type BSONObjectID = reactivemongo.bson.BSONObjectID
  val BSONObjectID: bson.BSONObjectID.type = reactivemongo.bson.BSONObjectID

  type BSONDateTime = reactivemongo.bson.BSONDateTime
  val BSONDateTime: bson.BSONDateTime.type = reactivemongo.bson.BSONDateTime

  type BSONArray = reactivemongo.bson.BSONArray
  val BSONArray: bson.BSONArray.type = reactivemongo.bson.BSONArray

  type BSONWriter[T, B <: BSONValue] = reactivemongo.bson.BSONWriter[T, B]
  type BSONReader[B <: BSONValue, T] = reactivemongo.bson.BSONReader[B, T]
  type BSONHandler[B <: BSONValue, T] = reactivemongo.bson.BSONHandler[B, T]

  type BSONDocumentWriter[T] = reactivemongo.bson.BSONDocumentWriter[T]
  type BSONDocumentReader[T] = reactivemongo.bson.BSONDocumentReader[T]
  type BSONDocumentHandler[T] = reactivemongo.bson.BSONDocumentHandler[T]

  val BSONMacros: Macros.type = reactivemongo.bson.Macros

}
