package com.lorandszakacs.sg.model.impl

import com.github.nscala_time.time.Imports._
import com.lorandszakacs.sg.model._
import org.joda.time.LocalDate
import reactivemongo.api.DB
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.Macros.Options.{SaveSimpleName, AllImplementations}
import reactivemongo.bson._

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

  protected implicit val modelNameBSON: BSONHandler[BSONString, ModelName] = new BSONHandler[BSONString, ModelName] {
    override def read(bson: BSONString): ModelName = ModelName(bson.value)

    override def write(t: ModelName): BSONString = BSONString(t.name)
  }

  protected implicit val localDateBSON: BSONHandler[BSONString, LocalDate] = new BSONHandler[BSONString, LocalDate] {
    private final val format = DateTimeFormat.forPattern("YYYY-MM-dd")

    override def write(t: LocalDate): BSONString = BSONString(t.toString(format))

    override def read(bson: BSONString): LocalDate = LocalDate.parse(bson.value, format)
  }

  protected implicit val dateTimeBSON: BSONHandler[BSONDateTime, DateTime] = new BSONHandler[BSONDateTime, DateTime] {
    override def write(t: DateTime): BSONDateTime = BSONDateTime(t.getMillis)

    override def read(bson: BSONDateTime): DateTime = new DateTime(bson.value)
  }

  protected implicit val photoBSON: BSONDocumentReader[Photo] with BSONDocumentWriter[Photo] with BSONHandler[BSONDocument, Photo] =
    Macros.handler[Photo]

  protected implicit val photoSetBSON: BSONDocumentReader[PhotoSet] with BSONDocumentWriter[PhotoSet] with BSONHandler[BSONDocument, PhotoSet] =
    new BSONDocumentReader[PhotoSet] with BSONDocumentWriter[PhotoSet] with BSONHandler[BSONDocument, PhotoSet] {
      val URL = "url"
      val Title = "title"
      val Date = "date"
      val Photos = "photos"

      override def write(t: PhotoSet): BSONDocument = BSONDocument(
        URL -> t.url,
        Title -> t.title,
        Date -> t.date,
        Photos -> t.photos
      )

      override def read(bson: BSONDocument): PhotoSet = PhotoSet(
        url = bson.getAsTry[String](URL).get,
        title = bson.getAsTry[String](Title).get,
        date = bson.getAsTry[LocalDate](Date).get,
        photos = bson.getAsTry[List[Photo]](Photos).get
      )
    }


  protected implicit val suicideGirlBSON: BSONDocumentReader[SuicideGirl] with BSONDocumentWriter[SuicideGirl] with BSONHandler[BSONDocument, SuicideGirl] =
    Macros.handler[SuicideGirl]

  protected implicit val hopefulBSON: BSONDocumentReader[Hopeful] with BSONDocumentWriter[Hopeful] with BSONHandler[BSONDocument, Hopeful] =
    Macros.handler[Hopeful]

  protected implicit val lastProcessedStatusBSON: BSONDocumentReader[LastProcessedIndex] with BSONDocumentWriter[LastProcessedIndex] with BSONHandler[BSONDocument, LastProcessedIndex] =
    Macros.handlerOpts[LastProcessedIndex, AllImplementations with SaveSimpleName]
}
