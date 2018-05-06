package com.lorandszakacs.sg.model

import java.net.URL

import com.lorandszakacs.util.time._
import com.lorandszakacs.util.mongodb.Annotations

/**
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 16 Mar 2015
  *
  */
object M {

  sealed trait MFactory[T <: M] {
    def apply(photoSetURL: URL, name: Name, photoSets: List[PhotoSet]): T

    def name: String
  }

  object SGFactory extends MFactory[SG] {
    override def apply(photoSetURL: URL, name: Name, photoSets: List[PhotoSet]): SG =
      SG(photoSetURL = photoSetURL, name = name, photoSets = photoSets.sortBy(_.date))

    override def name: String = "SG"
  }

  object HFFactory extends MFactory[HF] {
    override def apply(photoSetURL: URL, name: Name, photoSets: List[PhotoSet]): HF = {
      HF(photoSetURL = photoSetURL, name = name, photoSets = photoSets.sortBy(_.date))
    }

    override def name: String = "HF"
  }

}

/**
  *
  * @param all
  * is a union of [[sgs]] and [[hfs]]
  */
case class Ms(
  sgs: List[SG],
  hfs: List[HF],
  all: List[M]
) extends Product with Serializable {
  def newestM: Option[M] = all.headOption

  def ml(name: Name): Option[M] = all.find(_.name == name)

  def sg(name: Name): Option[SG] = sgs.find(_.name == name)

  def hf(name: Name): Option[HF] = hfs.find(_.name == name)

  def sgNames: List[Name] = sgs.map(_.name)

  def hfNames: List[Name] = hfs.map(_.name)

  def allNames: List[Name] = all.map(_.name)
}

sealed trait M extends Product with Serializable {
  type MType <: M

  def photoSetURL: URL

  def name: Name

  def photoSets: List[PhotoSet]

  def isHF: Boolean

  def isSG: Boolean

  def asSG: Option[SG]

  def makeSG: SG

  def asHF: Option[HF]

  def makeHF: HF

  def stringifyType: String

  def updatePhotoSets(newPhotoSets: List[PhotoSet]): MType

  final def numberOfSets: Int = photoSets.length

  final def numberOfPhotos: Int = photoSets.map(_.photos.length).sum

  final def photoSetsOldestFirst: List[PhotoSet] =
    this.photoSets.sortBy(_.date)

  final def photoSetsNewestFirst: List[PhotoSet] =
    this.photoSetsOldestFirst.reverse

  final def setsByNewestFirst: MType = updatePhotoSets(this.photoSets.sortBy(_.date).reverse)

  final def setsByOldestFirst: MType = updatePhotoSets(this.photoSets.sortBy(_.date))

  override def toString: String =
    s"""|---------${this.getClass.getSimpleName}: ${name.name} : ${photoSets.length}---------
        |url=${photoSetURL.toExternalForm}
        |${photoSetsNewestFirst.mkString("", "\n", "")}
        |""".stripMargin
}

object Name {

  def apply(name: String): Name = {
    new Name(name.trim.toLowerCase)
  }
}

final class Name private (
  val name: String
) {
  override def toString: String = s"Name($name)"

  def externalForm: String = s"${name.capitalize}"

  /**
    * HFs lose prefix, or suffix underscores in names
    * when they become SGs, therefore this is useful to determine
    * if one has become an SG.
    *
    * @return
    */
  def stripUnderscore: Name = Name(
    name.stripPrefix("_").stripPrefix("__").stripPrefix("___").stripSuffix("_").stripSuffix("__").stripSuffix("___")
  )

  override def equals(other: Any): Boolean = other match {
    case that: Name =>
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

final class PhotoSetTitle private (
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

final case class SG(
  photoSetURL:                  URL,
  @Annotations.Key("_id") name: Name,
  photoSets:                    List[PhotoSet]
) extends M with Product with Serializable {
  override type MType = SG

  override def updatePhotoSets(newPhotoSets: List[PhotoSet]): SG = this.copy(photoSets = newPhotoSets)

  override def isHF: Boolean = false

  override def isSG: Boolean = true

  override def asSG: Option[SG] = Option(this)

  override def makeSG: SG = this

  override def asHF: Option[HF] = None

  override def makeHF: HF = throw new AssertionError("attempted to cast a SG to a HF")

  override def stringifyType: String = "SG"
}

final case class HF(
  photoSetURL:                  URL,
  @Annotations.Key("_id") name: Name,
  photoSets:                    List[PhotoSet]
) extends M with Product with Serializable {

  override type MType = HF

  override def updatePhotoSets(newPhotoSets: List[PhotoSet]): HF = this.copy(photoSets = newPhotoSets)

  override def isHF: Boolean = true

  override def isSG: Boolean = false

  override def asSG: Option[SG] = None

  override def makeSG: SG = throw new AssertionError("attempted to cast HF as SG")

  override def asHF: Option[HF] = Option(this)

  override def makeHF: HF = this

  override def stringifyType: String = "HF"

}

final case class PhotoSet(
  url:    URL,
  title:  PhotoSetTitle,
  date:   LocalDate,
  photos: List[Photo] = Nil,
  @Annotations.Ignore()
  isHFSet: Option[Boolean] = None
) extends Product with Serializable {

  def id: String = url.toExternalForm

  override def toString: String =
    s"""
       |title = ${title.name}
       |date  = ${Util.dateTimeFormat.format(date)}
       |url   = ${url.toExternalForm}
       |${isHFSet.map(b => s"isHF  = $b").getOrElse("")}
       |${photos.mkString("{\n\t", "\n\t", "\n}")}
       |${"_________________"}
      """.stripMargin
}

final case class Photo(
  url:          URL,
  thumbnailURL: URL,
  index:        Int
) extends Product with Serializable {

  override def toString: String = s"$url :: $thumbnailURL"
}

private[model] object Util {
  final val dateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd")
}
