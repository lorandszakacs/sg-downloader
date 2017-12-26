package com.lorandszakacs.sg.model.impl

import com.lorandszakacs.sg.model._
import com.lorandszakacs.util.mongodb._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 14 Jul 2017
  *
  */
private[impl] trait IndexSingleDocRepo[T] extends SingleDocumentMongoCollection[T, String, BSONString] {

  protected implicit val nameBSONFFS: BSONHandler[BSONString, Name] = SGRepoBSON.nameBSON

  override protected implicit val idHandler: BSONHandler[BSONString, String] =
    BSONStringHandler

  override val collectionName: String = "indexes"

}
