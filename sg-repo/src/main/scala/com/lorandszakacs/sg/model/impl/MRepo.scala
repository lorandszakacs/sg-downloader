package com.lorandszakacs.sg.model.impl

import com.lorandszakacs.sg.model._
import com.lorandszakacs.util.mongodb._
import com.lorandszakacs.util.future._
import com.lorandszakacs.util.time._
import SGRepoBSON._
import com.lorandszakacs.util.math.Identifier

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 14 Jul 2017
  *
  */
private[impl] abstract class MRepo[Content <: M](override protected val identifier: Identifier[Content, Name])
    extends MongoCollection[Content, Name, BSONString] {

  override protected implicit val idHandler: BSONHandler[BSONString, Name] =
    nameBSON

  final def findBetweenDays(start: LocalDate, end: LocalDate): IO[List[Content]] = {
    val q = document(
      "photoSets.date" -> document($gte -> start),
      "photoSets.date" -> document($lte -> end)
    )
    this.findMany(q)
  }

}
