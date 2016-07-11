package com.lorandszakacs.sg.model

import java.net.URL

import com.github.nscala_time.time.Imports._

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
      SuicideGirl(photoSetURL = photoSetURL, name = name, photoSets = photoSets)

    override def name: String = "suicide girl"
  }

  object HopefulFactory extends ModelFactory[Hopeful] {
    override def apply(photoSetURL: URL, name: ModelName, photoSets: List[PhotoSet]): Hopeful = {
      Hopeful(photoSetURL = photoSetURL, name = name, photoSets = photoSets)
    }

    override def name: String = "hopeful"
  }

}

sealed trait Model {
  def photoSetURL: URL

  def name: ModelName

  def photoSets: List[PhotoSet]

  def isHopeful: Boolean

  def isSuicideGirl: Boolean

  def asSuicideGirls: Option[SuicideGirl]

  def asHopeful: Option[Hopeful]

  def stringifyType: String

  final def numberOfSets: Int = photoSets.length

  final def numberOfPhotos: Int = photoSets.map(_.photos.length).sum

  override def toString =
    s"""|---------${this.getClass.getSimpleName}: ${name.name} : ${photoSets.length}---------
        |url=${photoSetURL.toExternalForm}
        |${photoSets.mkString("", "\n", "")}
        |""".stripMargin
}

sealed trait ModelUpdater[T <: Model] {
  this: Model =>
  def updatePhotoSets(newPhotoSets: List[PhotoSet]): T

  final def reverseSets: T = updatePhotoSets(this.photoSets.reverse)

  final def addPhotoSet(ph: PhotoSet): T = {
    if (photoSets.exists(_.id == ph.id)) {
      throw PhotoSetAlreadyExistsException(name, ph)
    } else {
      updatePhotoSets(ph :: this.photoSets)
    }
  }

  final def updatePhotoSet(ph: PhotoSet): T = {
    if (!photoSets.exists(_.id == ph.id)) {
      throw PhotoSetDoesNotExistException(name, ph)
    } else {
      val newPHS = photoSets map { oldPH =>
        if (oldPH == ph) ph else oldPH
      }
      updatePhotoSets(newPHS)
    }
  }

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

  override def asSuicideGirls: Option[SuicideGirl] = Option(this)

  override def asHopeful: Option[Hopeful] = None

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

  override def asSuicideGirls: Option[SuicideGirl] = None

  override def asHopeful: Option[Hopeful] = Option(this)

  override def stringifyType: String = "hopeful"
}

final case class PhotoSet(
  url: URL,
  title: PhotoSetTitle,
  date: LocalDate,
  photos: List[Photo] = Nil
) {

  def id: String = url.toExternalForm

  override def toString =
    s"""
       |title = ${title.name}
       |date  = ${date.toString(Util.dateTimeFormat)}
       |url   = ${url.toExternalForm}
       |${photos.mkString("{\n\t", "\n\t", "\n}")}
       |${"_________________"}
      """.stripMargin
}

final case class Photo(
  url: URL,
  index: Int
) {

  override def toString = s"$url"

  private def digitFormat(n: Int) = if (n < 10) s"0$n" else "%2d".format(n)
}

private[model] object Util {
  final val dateTimeFormat = DateTimeFormat.forPattern("YYYY-MM-dd")
}