package com.lorandszakacs.sg.model

import com.github.nscala_time.time.Imports._

/**
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 16 Mar 2015
  *
  */

sealed trait Model {
  def photoSetURI: String

  def name: ModelName

  def photoSets: List[PhotoSet]

  def isHopeful: Boolean

  def isSuicideGirl: Boolean

  override def toString =
    s"""
        ---------${this.getClass.getSimpleName}: ${name.name} : ${photoSets.length}---------
        uri=$photoSetURI
        ${photoSets.mkString("", "\n", "")}
      """.stripMargin(' ')
}

sealed trait ModelUpdater[T <: Model] {
  this: Model =>
  def updatePhotoSets(newPhotoSets: List[PhotoSet]): T

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
  photoSetURI: String,
  name: ModelName,
  photoSets: List[PhotoSet]
) extends Model with ModelUpdater[SuicideGirl] {

  override def updatePhotoSets(newPhotoSets: List[PhotoSet]): SuicideGirl = this.copy(photoSets = newPhotoSets)

  override def isHopeful: Boolean = false

  override def isSuicideGirl: Boolean = true
}

final case class Hopeful(
  photoSetURI: String,
  name: ModelName,
  photoSets: List[PhotoSet]
) extends Model with ModelUpdater[Hopeful] {

  override def updatePhotoSets(newPhotoSets: List[PhotoSet]): Hopeful = this.copy(photoSets = newPhotoSets)

  override def isHopeful: Boolean = true

  override def isSuicideGirl: Boolean = false
}

final case class PhotoSet private(
  url: String,
  title: PhotoSetTitle,
  date: LocalDate,
  photos: List[Photo] = Nil
) {

  def id: String = url

  def updateURL(newURL: String): PhotoSet = this.copy(url = newURL)

  override def toString =
    s"""
       |${"\t"}title = $title
       |${"\t"}date  = ${date.toString(Util.dateTimeFormat)}
       |${"\t"}uri   = ${url.toString}
       |${photos.mkString("\t{\n", "\t\t\n", "\n\t}")}
       |${"\t_________________"}
      """.stripMargin
}

final case class Photo(
  uri: String,
  index: Int
) {

  override def toString = s"\t\t${digitFormat(index)} -> $uri"

  private def digitFormat(n: Int) = if (n < 10) s"0$n" else "%2d".format(n)
}

private[model] object Util {
  final val dateTimeFormat = DateTimeFormat.forPattern("YYYY-MM-dd")
}