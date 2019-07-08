package com.lorandszakacs.util.mongodb

import reactivemongo.bson
import reactivemongo.bson.Macros

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 14 Jul 2017
  *
  */
trait MongoDBBSONTypes {
  type BSONCollection = reactivemongo.api.collections.bson.BSONCollection

  type BSONValue    = bson.BSONValue
  type BSONDocument = bson.BSONDocument
  val BSONDocument: bson.BSONDocument.type = bson.BSONDocument

  type BSONString = bson.BSONString
  val BSONString: bson.BSONString.type = bson.BSONString

  type BSONInteger = bson.BSONInteger
  val BSONInteger: bson.BSONInteger.type = bson.BSONInteger

  type BSONObjectID = bson.BSONObjectID
  val BSONObjectID: bson.BSONObjectID.type = bson.BSONObjectID

  type BSONDateTime = bson.BSONDateTime
  val BSONDateTime: bson.BSONDateTime.type = bson.BSONDateTime

  type BSONArray = bson.BSONArray
  val BSONArray: bson.BSONArray.type = bson.BSONArray

  type BSONWriter[T, B <: BSONValue]  = bson.BSONWriter[T, B]
  type BSONReader[B <: BSONValue, T]  = bson.BSONReader[B, T]
  type BSONHandler[B <: BSONValue, T] = bson.BSONHandler[B, T]

  type BSONDocumentWriter[T]  = bson.BSONDocumentWriter[T]
  type BSONDocumentReader[T]  = bson.BSONDocumentReader[T]
  type BSONDocumentHandler[T] = bson.BSONDocumentHandler[T]

  val BSONMacros:  Macros.type             = bson.Macros
  val Annotations: Macros.Annotations.type = bson.Macros.Annotations

  def document: BSONDocument =
    bson.document

  def document(elements: bson.Producer[reactivemongo.bson.BSONElement]*): BSONDocument =
    bson.document(elements: _*)

  def array: BSONArray =
    bson.array

  def array(values: reactivemongo.bson.Producer[reactivemongo.bson.BSONValue]*): BSONArray =
    bson.array(values: _*)
}
