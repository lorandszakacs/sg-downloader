package com.lorandszakacs.sg.model.impl

import java.net.URL

import com.github.nscala_time.time.Imports._
import com.lorandszakacs.sg.model._
import org.joda.time.LocalDate
import com.lorandszakacs.util.mongodb._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 14 Jul 2017
  *
  */
private[impl] object SGRepoBSON extends SGRepoBSON

private[impl] trait SGRepoBSON {

  def nameBSON: BSONHandler[BSONString, Name] = new BSONHandler[BSONString, Name] {
    override def read(bson: BSONString): Name = Name(bson.value)

    override def write(t: Name): BSONString = BSONString(t.name)
  }

  implicit val photoSetTitleBSON: BSONHandler[BSONString, PhotoSetTitle] = new BSONHandler[BSONString, PhotoSetTitle] {
    override def read(bson: BSONString): PhotoSetTitle = PhotoSetTitle(bson.value)

    override def write(t: PhotoSetTitle): BSONString = BSONString(t.name)
  }

  implicit val localDateBSON: BSONHandler[BSONString, LocalDate] = new BSONHandler[BSONString, LocalDate] {
    private final val format = DateTimeFormat.forPattern("YYYY-MM-dd")

    override def write(t: LocalDate): BSONString = BSONString(t.toString(format))

    override def read(bson: BSONString): LocalDate = LocalDate.parse(bson.value, format)
  }

  implicit val dateTimeBSON: BSONHandler[BSONDateTime, DateTime] = new BSONHandler[BSONDateTime, DateTime] {
    override def write(t: DateTime): BSONDateTime = BSONDateTime(t.getMillis)

    override def read(bson: BSONDateTime): DateTime = new DateTime(bson.value)
  }

  implicit val urlBSON: BSONReader[BSONString, URL] with BSONWriter[URL, BSONString] with BSONHandler[BSONString, URL] =
    new BSONReader[BSONString, URL] with BSONWriter[URL, BSONString] with BSONHandler[BSONString, URL] {
      override def read(bson: BSONString): URL = new URL(bson.value)

      override def write(t: URL): BSONString = BSONString(t.toExternalForm)
    }

  implicit val photoBSON
    : BSONDocumentReader[Photo] with BSONDocumentWriter[Photo] with BSONHandler[BSONDocument, Photo] =
    BSONMacros.handler[Photo]

  implicit val photoSetBSON
    : BSONDocumentReader[PhotoSet] with BSONDocumentWriter[PhotoSet] with BSONHandler[BSONDocument, PhotoSet] =
    BSONMacros.handler[PhotoSet]

}
