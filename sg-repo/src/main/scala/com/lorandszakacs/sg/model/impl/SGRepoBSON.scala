package com.lorandszakacs.sg.model.impl

import com.lorandszakacs.sg.model._
import com.lorandszakacs.util.mongodb._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 14 Jul 2017
  *
  */
private[impl] object SGRepoBSON extends SGRepoBSON

private[impl] trait SGRepoBSON extends UtilBsonHandlers {

  def nameBSON: BSONHandler[BSONString, Name] = new BSONHandler[BSONString, Name] {
    override def read(bson: BSONString): Name = Name(bson.value)

    override def write(t: Name): BSONString = BSONString(t.name)
  }

  implicit val photoSetTitleBSON: BSONHandler[BSONString, PhotoSetTitle] = new BSONHandler[BSONString, PhotoSetTitle] {
    override def read(bson: BSONString): PhotoSetTitle = PhotoSetTitle(bson.value)

    override def write(t: PhotoSetTitle): BSONString = BSONString(t.name)
  }

  implicit val photoBSON
    : BSONDocumentReader[Photo] with BSONDocumentWriter[Photo] with BSONHandler[BSONDocument, Photo] =
    BSONMacros.handler[Photo]

  implicit val photoSetBSON
    : BSONDocumentReader[PhotoSet] with BSONDocumentWriter[PhotoSet] with BSONHandler[BSONDocument, PhotoSet] =
    BSONMacros.handler[PhotoSet]

}
