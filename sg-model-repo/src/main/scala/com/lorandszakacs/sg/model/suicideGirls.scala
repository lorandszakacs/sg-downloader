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
    if (photoSets.exists(_.canonicalId == ph.canonicalId)) {
      throw PhotoSetAlreadyExistsException(name, ph)
    } else {
      updatePhotoSets(ph :: this.photoSets)
    }
  }

  final def updatePhotoSet(ph: PhotoSet): T = {
    if (!photoSets.exists(_.canonicalId == ph.canonicalId)) {
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

final case class PhotoSet(
  uri: String,
  title: String,
  photos: List[Photo],
  date: DateTime
) {

  def canonicalId: String = uri

  override lazy val toString =
    s"""
        ${"\t"}title = $title
        ${"\t"}date  = ${date.toString(Util.dateTimeFormat)}
        ${"\t"}uri   = ${uri.toString}
        ${"\t_________________"}
        ${photos.mkString("", "\t\t\n", "")}
        ${"\t================="}
      """.stripMargin(' ')
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