package com.lorandszakacs.util.mongodb

import java.net.URL

import com.lorandszakacs.util.time._

/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 06 May 2018
  *
  */
object UtilBsonHandlers extends UtilBsonHandlers

trait UtilBsonHandlers {

  implicit val localDateBSON: BSONHandler[BSONString, LocalDate] = new BSONHandler[BSONString, LocalDate] {
    override def write(t: LocalDate): BSONString = BSONString(TimeUtil.localDateFormat.format(t))

    override def read(bson: BSONString): LocalDate =
      LocalDate.parse(bson.value, TimeUtil.localDateFormat)

  }

  implicit val dateTimeBSON: BSONHandler[BSONDateTime, Instant] = new BSONHandler[BSONDateTime, Instant] {
    override def write(t: Instant): BSONDateTime = BSONDateTime(t.toEpochMilli)

    override def read(bson: BSONDateTime): Instant = java.time.Instant.ofEpochMilli(bson.value)
  }

  implicit val urlBSON: BSONReader[BSONString, URL] with BSONWriter[URL, BSONString] with BSONHandler[BSONString, URL] =
    new BSONReader[BSONString, URL] with BSONWriter[URL, BSONString] with BSONHandler[BSONString, URL] {
      override def read(bson: BSONString): URL = new URL(bson.value)

      override def write(t: URL): BSONString = BSONString(t.toExternalForm)
    }

}
