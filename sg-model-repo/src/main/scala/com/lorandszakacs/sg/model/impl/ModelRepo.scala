package com.lorandszakacs.sg.model.impl

import com.lorandszakacs.sg.model._
import com.lorandszakacs.util.mongodb._
import com.lorandszakacs.util.future._
import com.lorandszakacs.util.time._
import ModelBSON._
import com.lorandszakacs.util.math.Identifier

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 14 Jul 2017
  *
  */
private[impl] abstract class ModelRepo[M <: Model]
(override protected val identifier: Identifier[M, ModelName])
  extends MongoCollection[M, ModelName, BSONString] {

  override protected implicit val idHandler: BSONHandler[BSONString, ModelName] =
    modelNameBSON

  def findWithZeroSets: Future[List[M]] = {
    val q: BSONDocument = document(
      "photoSets" -> BSONDocument($size -> 0)
    )
    this.findMany(q)
  }

  def findBetweenDays(start: LocalDate, end: LocalDate): Future[List[Model]] = {
    val q = document(
      "photoSets.date" -> document($gte -> start),
      "photoSets.date" -> document($lte -> end)
    )
    this.findMany(q)
  }

}
