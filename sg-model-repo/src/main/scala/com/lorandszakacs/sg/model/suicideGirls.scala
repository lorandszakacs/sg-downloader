package com.lorandszakacs.sg.model

import java.net.URL

import com.github.nscala_time.time.Imports._
import org.joda.time.format.DateTimeFormatter
import reactivemongo.bson.Macros.Annotations.Ignore

import scala.language.postfixOps

/**
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 16 Mar 2015
  *
  */
object Model {

  sealed trait ModelFactory[T <: Model] {
    def apply(photoSetURL: URL, name: ModelName, photoSets: List[PhotoSet]): T

    def name: String
  }

  object SuicideGirlFactory extends ModelFactory[SuicideGirl] {
    override def apply(photoSetURL: URL, name: ModelName, photoSets: List[PhotoSet]): SuicideGirl =
      SuicideGirl(photoSetURL = photoSetURL, name = name, photoSets = photoSets.sortBy(_.date))

    override def name: String = "suicide girl"
  }

  object HopefulFactory extends ModelFactory[Hopeful] {
    override def apply(photoSetURL: URL, name: ModelName, photoSets: List[PhotoSet]): Hopeful = {
      Hopeful(photoSetURL = photoSetURL, name = name, photoSets = photoSets.sortBy(_.date))
    }

    override def name: String = "hopeful"
  }

}

/**
  *
  * @param all
  * is a union of [[sgs]] and [[hfs]]
  */
case class Models(
  sgs: List[SuicideGirl],
  hfs: List[Hopeful],
  all: List[Model]
) {
  def newestModel: Option[Model] = all.headOption

  def ml(name: ModelName): Option[Model] = all.find(_.name == name)

  def sg(name: ModelName): Option[SuicideGirl] = sgs.find(_.name == name)

  def hf(name: ModelName): Option[Hopeful] = hfs.find(_.name == name)

  def sgNames: List[ModelName] = sgs.map(_.name)

  def hfNames: List[ModelName] = hfs.map(_.name)

  def allNames: List[ModelName] = all.map(_.name)
}

sealed trait Model {
  def photoSetURL: URL

  def name: ModelName

  def photoSets: List[PhotoSet]

  def isHopeful: Boolean

  def isSuicideGirl: Boolean

  def asSuicideGirl: Option[SuicideGirl]

  def makeSuicideGirl: SuicideGirl

  def asHopeful: Option[Hopeful]

  def makeHopeful: Hopeful

  def stringifyType: String

  final def numberOfSets: Int = photoSets.length

  final def numberOfPhotos: Int = photoSets.map(_.photos.length).sum

  final def photoSetsOldestFirst: List[PhotoSet] =
    this.photoSets.sortBy(_.date)

  final def photoSetsNewestFirst: List[PhotoSet] =
    this.photoSetsOldestFirst.reverse

  override def toString: String =
    s"""|---------${this.getClass.getSimpleName}: ${name.name} : ${photoSets.length}---------
        |url=${photoSetURL.toExternalForm}
        |${photoSetsNewestFirst.mkString("", "\n", "")}
        |""".stripMargin
}

sealed trait ModelUpdater[T <: Model] {
  this: Model =>
  def updatePhotoSets(newPhotoSets: List[PhotoSet]): T

  final def setsByNewestFirst: T = updatePhotoSets(this.photoSets.sortBy(_.date).reverse)

  final def setsByOldestFirst: T = updatePhotoSets(this.photoSets.sortBy(_.date))
}

object ModelName {
  def apply(name: String): ModelName = {
    new ModelName(name.trim.toLowerCase)
  }
}

final class ModelName private(
  val name: String
) {
  override def toString: String = s"ModelName($name)"

  def externalForm: String = s"${name.capitalize}"

  /**
    * Hopefuls lose prefix, or suffix underscores in names
    * when they become SGs, therefore this is useful to determine
    * if one has become an SG.
    *
    * @return
    */
  def stripUnderscore: ModelName = ModelName(name.stripPrefix("_").stripPrefix("__").stripSuffix("_").stripSuffix("__"))

  override def equals(other: Any): Boolean = other match {
    case that: ModelName =>
      name == that.name
    case _ => false
  }

  override def hashCode(): Int = {
    name.hashCode * 31
  }
}

object PhotoSetTitle {
  def apply(name: String): PhotoSetTitle = {
    new PhotoSetTitle(name.trim.toUpperCase.replace("  ", "").replace("\t", " "))
  }
}

final class PhotoSetTitle private(
  val name: String
) {
  override def toString: String = s"PhotoSetTitle($name)"

  def externalForm: String = s"${name.toLowerCase.capitalize}"

  override def equals(other: Any): Boolean = other match {
    case that: PhotoSetTitle =>
      name == that.name
    case _ => false
  }

  override def hashCode(): Int = {
    name.hashCode * 31
  }
}

final case class SuicideGirl(
  photoSetURL: URL,
  name: ModelName,
  photoSets: List[PhotoSet]
) extends Model with ModelUpdater[SuicideGirl] {

  override def updatePhotoSets(newPhotoSets: List[PhotoSet]): SuicideGirl = this.copy(photoSets = newPhotoSets)

  override def isHopeful: Boolean = false

  override def isSuicideGirl: Boolean = true

  override def asSuicideGirl: Option[SuicideGirl] = Option(this)

  override def makeSuicideGirl: SuicideGirl = this

  override def asHopeful: Option[Hopeful] = None

  override def makeHopeful: Hopeful = throw new AssertionError("attempted to cast a SuicideGirl to a Hopeful")

  override def stringifyType: String = "suicide girl"
}

final case class Hopeful(
  photoSetURL: URL,
  name: ModelName,
  photoSets: List[PhotoSet]
) extends Model with ModelUpdater[Hopeful] {

  override def updatePhotoSets(newPhotoSets: List[PhotoSet]): Hopeful = this.copy(photoSets = newPhotoSets)

  override def isHopeful: Boolean = true

  override def isSuicideGirl: Boolean = false

  override def asSuicideGirl: Option[SuicideGirl] = None

  override def makeSuicideGirl: SuicideGirl = throw new AssertionError("attempted to cast Hopeful as SuicideGirl")

  override def asHopeful: Option[Hopeful] = Option(this)

  override def makeHopeful: Hopeful = this

  override def stringifyType: String = "hopeful"

}

final case class PhotoSet(
  url: URL,
  title: PhotoSetTitle,
  date: LocalDate,
  photos: List[Photo] = Nil,
  @Ignore() isHopefulSet: Option[Boolean] = None
) {

  def id: String = url.toExternalForm

  override def toString: String =
    s"""
       |title = ${title.name}
       |date  = ${date.toString(Util.dateTimeFormat)}
       |url   = ${url.toExternalForm}
       |${isHopefulSet.map(b => s"isHF  = $b").getOrElse("")}
       |${photos.mkString("{\n\t", "\n\t", "\n}")}
       |${"_________________"}
      """.stripMargin
}

final case class Photo(
  url: URL,
  thumbnailURL: URL,
  index: Int
) {

  override def toString: String = s"$url :: $thumbnailURL"
}

private[model] object Util {
  final val dateTimeFormat: DateTimeFormatter = DateTimeFormat.forPattern("YYYY-MM-dd")
}