package com.lorandszakacs.sg.model.impl

import com.github.nscala_time.time.Imports._
import com.lorandszakacs.sg.model._
import org.joda.time.LocalDate
import reactivemongo.api.DB
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.Macros.Options.{SaveSimpleName, SimpleAllImplementations}
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

  protected implicit val suicideGirlsIndexBSON: BSONDocumentReader[SuicideGirlIndex] with BSONDocumentWriter[SuicideGirlIndex] with BSONHandler[BSONDocument, SuicideGirlIndex] =
    Macros.handler[SuicideGirlIndex]

  protected implicit val hopefulIndexBSON: BSONDocumentReader[HopefulIndex] with BSONDocumentWriter[HopefulIndex] with BSONHandler[BSONDocument, HopefulIndex] =
    Macros.handler[HopefulIndex]

  protected implicit val photoSetTitleBSON: BSONHandler[BSONString, PhotoSetTitle] = new BSONHandler[BSONString, PhotoSetTitle] {
    override def read(bson: BSONString): PhotoSetTitle = PhotoSetTitle(bson.value)

    override def write(t: PhotoSetTitle): BSONString = BSONString(t.name)
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
    Macros.handler[PhotoSet]


  protected implicit val suicideGirlBSON: BSONDocumentReader[SuicideGirl] with BSONDocumentWriter[SuicideGirl] with BSONHandler[BSONDocument, SuicideGirl] =
    Macros.handler[SuicideGirl]

  protected implicit val hopefulBSON: BSONDocumentReader[Hopeful] with BSONDocumentWriter[Hopeful] with BSONHandler[BSONDocument, Hopeful] =
    Macros.handler[Hopeful]

  protected implicit val lastProcessedHopefulBSON: BSONDocumentReader[LastProcessedHopeful] with BSONDocumentWriter[LastProcessedHopeful] with BSONHandler[BSONDocument, LastProcessedHopeful] =
    Macros.handlerOpts[LastProcessedHopeful, SaveSimpleName]

  implicit val lastProcessedSuicideGirlBSON: BSONDocumentReader[LastProcessedSG] with BSONDocumentWriter[LastProcessedSG] with BSONHandler[BSONDocument, LastProcessedSG] =
    Macros.handlerOpts[LastProcessedSG, SaveSimpleName]
}
