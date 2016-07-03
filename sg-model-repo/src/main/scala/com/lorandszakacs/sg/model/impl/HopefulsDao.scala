package com.lorandszakacs.sg.model.impl

import reactivemongo.api.DB
import reactivemongo.api.collections.bson.BSONCollection

import scala.concurrent.ExecutionContext

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
private[model] class HopefulsDao(val db: DB)(implicit val ec: ExecutionContext) {
  private val collectionName: String = "hopefuls"

  private val collection: BSONCollection = db(collectionName)
}