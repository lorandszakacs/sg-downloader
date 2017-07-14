package com.lorandszakacs.sg.model.impl

import java.net.URL

import com.github.nscala_time.time.Imports._
import com.lorandszakacs.sg.model._
import org.joda.time.LocalDate
import com.lorandszakacs.util.mongodb._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
@scala.deprecated("use repo", "now")
private[impl] trait SGRepoBSON {
  protected val SuicideGirlsIndexId = "suicide-girls-index"
  protected val HopefulIndexId = "hopefuls-index"
  protected val LastProcessedId = "last-processed"
  protected val CleanedUpIndexId = "cleaned-up-index"

  protected implicit val modelNameBSON: BSONHandler[BSONString, ModelName] = new BSONHandler[BSONString, ModelName] {
    override def read(bson: BSONString): ModelName = ModelName(bson.value)

    override def write(t: ModelName): BSONString = BSONString(t.name)
  }

  protected implicit val suicideGirlsIndexBSON: BSONDocumentReader[SuicideGirlIndex] with BSONDocumentWriter[SuicideGirlIndex] with BSONHandler[BSONDocument, SuicideGirlIndex] =
    new BSONDocumentReader[SuicideGirlIndex] with BSONDocumentWriter[SuicideGirlIndex] with BSONHandler[BSONDocument, SuicideGirlIndex] {
      private val handler = BSONMacros.handler[SuicideGirlIndex]

      override def write(t: SuicideGirlIndex): BSONDocument = BSONDocument(_id -> SuicideGirlsIndexId) ++ handler.write(t)

      override def read(bson: BSONDocument): SuicideGirlIndex = handler.read(bson)
    }


  protected implicit val hopefulIndexBSON: BSONDocumentReader[HopefulIndex] with BSONDocumentWriter[HopefulIndex] with BSONHandler[BSONDocument, HopefulIndex] =
    new BSONDocumentReader[HopefulIndex] with BSONDocumentWriter[HopefulIndex] with BSONHandler[BSONDocument, HopefulIndex] {
      private val handler = BSONMacros.handler[HopefulIndex]

      override def write(t: HopefulIndex): BSONDocument = BSONDocument(_id -> HopefulIndexId) ++ handler.write(t)

      override def read(bson: BSONDocument): HopefulIndex = handler.read(bson)
    }

  protected implicit val cleanedUpIndexBSON: BSONDocumentReader[CleanedUpModelsIndex] with BSONDocumentWriter[CleanedUpModelsIndex] with BSONHandler[BSONDocument, CleanedUpModelsIndex] =
    new BSONDocumentReader[CleanedUpModelsIndex] with BSONDocumentWriter[CleanedUpModelsIndex] with BSONHandler[BSONDocument, CleanedUpModelsIndex] {
      private val handler = BSONMacros.handler[CleanedUpModelsIndex]

      override def write(t: CleanedUpModelsIndex): BSONDocument = BSONDocument(_id -> CleanedUpIndexId) ++ handler.write(t)

      override def read(bson: BSONDocument): CleanedUpModelsIndex = handler.read(bson)
    }


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

  protected implicit val urlBSON: BSONReader[BSONString, URL] with BSONWriter[URL, BSONString] with BSONHandler[BSONString, URL] =
    new BSONReader[BSONString, URL] with BSONWriter[URL, BSONString] with BSONHandler[BSONString, URL] {
      override def read(bson: BSONString): URL = new URL(bson.value)

      override def write(t: URL): BSONString = BSONString(t.toExternalForm)
    }

  protected implicit val photoBSON: BSONDocumentReader[Photo] with BSONDocumentWriter[Photo] with BSONHandler[BSONDocument, Photo] =
    BSONMacros.handler[Photo]

  protected implicit val photoSetBSON: BSONDocumentReader[PhotoSet] with BSONDocumentWriter[PhotoSet] with BSONHandler[BSONDocument, PhotoSet] =
    BSONMacros.handler[PhotoSet]

  protected implicit val suicideGirlBSON: BSONDocumentReader[SuicideGirl] with BSONDocumentWriter[SuicideGirl] with BSONHandler[BSONDocument, SuicideGirl] =
    new BSONDocumentReader[SuicideGirl] with BSONDocumentWriter[SuicideGirl] with BSONHandler[BSONDocument, SuicideGirl] {
      private val handler = BSONMacros.handler[SuicideGirl]

      override def write(t: SuicideGirl): BSONDocument = handler.write(t) ++ (_id -> t.name)

      override def read(bson: BSONDocument): SuicideGirl = handler.read(bson)
    }

  protected implicit val hopefulBSON: BSONDocumentReader[Hopeful] with BSONDocumentWriter[Hopeful] with BSONHandler[BSONDocument, Hopeful] =
    new BSONDocumentReader[Hopeful] with BSONDocumentWriter[Hopeful] with BSONHandler[BSONDocument, Hopeful] {
      private val handler: BSONDocumentReader[Hopeful] with BSONDocumentWriter[Hopeful] with BSONHandler[BSONDocument, Hopeful] = BSONMacros.handler[Hopeful]

      override def write(t: Hopeful): BSONDocument = handler.write(t) ++ (_id -> t.name)

      override def read(bson: BSONDocument): Hopeful = handler.read(bson)
    }

  implicit val lastProcessedMarkerBSON: BSONDocumentHandler[LastProcessedMarker] = BSONMacros.handler[LastProcessedMarker]
}

@scala.deprecated("use repo", "now")
private[impl] trait MongoDAO extends SGRepoBSON {
  protected def db: Database

  protected def collectionName: String

  protected lazy val collection: BSONCollection = db(collectionName)

}
