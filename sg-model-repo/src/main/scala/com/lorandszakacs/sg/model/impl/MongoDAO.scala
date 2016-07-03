package com.lorandszakacs.sg.model.impl

import reactivemongo.api.DB
import reactivemongo.api.collections.bson.BSONCollection

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
private[impl] trait MongoDAO {
  protected val _id = "_id"

  protected def db: DB

  protected def collectionName: String

  protected lazy val collection: BSONCollection = db(collectionName)

}
