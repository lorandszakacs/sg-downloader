package com.lorandszakacs.sg.model

import com.github.nscala_time.time.Imports._

/**
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 16 Mar 2015
  *
  */

sealed trait Model[T <: Model[T]] {
  def uri: String

  def name: String

  def photoSets: List[PhotoSet]

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


  override def toString =
    s"""
        ---------$name:${photoSets.length}---------
        uri=$uri
        ${photoSets.mkString("", "\n", "")}
      """.stripMargin(' ')
}


final case class SuicideGirl(
  uri: String,
  name: String,
  photoSets: List[PhotoSet]
) extends Model[SuicideGirl] {

  override def updatePhotoSets(newPhotoSets: List[PhotoSet]): SuicideGirl = this.copy(photoSets = newPhotoSets)
}

final case class Hopeful(
  uri: String,
  name: String,
  photoSets: List[PhotoSet]
) extends Model[Hopeful] {

  override def updatePhotoSets(newPhotoSets: List[PhotoSet]): Hopeful = this.copy(photoSets = newPhotoSets)
}

object PhotoSet extends ((String, String, LocalDate, List[Photo]) => PhotoSet) {
  def apply(url: String, title: String, date: LocalDate, photos: List[Photo] = Nil) = {
    new PhotoSet(url.trim, title.toUpperCase, date, photos)
  }
}

final class PhotoSet private(
  val url: String,
  val title: String,
  val date: LocalDate,
  val photos: List[Photo]
) {

  def id: String = url

  def updateURL(newURL: String): PhotoSet = this.copy(url = newURL)

  def copy(url: String = url, title: String = title, date: LocalDate = date, photos: List[Photo] = photos): PhotoSet = {
    new PhotoSet(url, title, date, photos)
  }

  override def toString =
    s"""
        ${"\t"}title = $title
        ${"\t"}date  = ${date.toString(Util.dateTimeFormat)}
        ${"\t"}uri   = ${url.toString}
        ${"\t_________________"}
        ${photos.mkString("", "\t\t\n", "")}
        ${"\t================="}
      """.stripMargin(' ')

  override def equals(other: Any): Boolean = other match {
    case that: PhotoSet =>
      url == that.url &&
        title == that.title &&
        date == that.date &&
        photos == that.photos
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(url, title, date, photos)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
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