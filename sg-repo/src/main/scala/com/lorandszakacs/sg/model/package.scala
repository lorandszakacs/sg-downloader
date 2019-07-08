package com.lorandszakacs.sg

import com.lorandszakacs.util.math.{Identifier, Identity}

import scala.language.implicitConversions

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
package object model {

  implicit val mIdentity: Identity[M] = Identity[M] { (m1, m2) =>
    m1.name == m2.name
  }

  implicit val sgIdentifier: Identifier[SG, Name] = Identifier[SG, Name] { m: SG =>
    m.name
  }
  implicit val hfIdentifier: Identifier[HF, Name] = Identifier[HF, Name] { m: HF =>
    m.name
  }

  implicit class StringBuffedWithName(str: String) {
    def toName: Name = Name(str)

    def toTitleName: PhotoSetTitle = PhotoSetTitle(str)
  }

  implicit class BuffedMs(ms: List[M]) {

    def group: Ms = {
      val (sgs, hf) = ms.partition(_.isSG)
      Ms(
        sgs = sgs.map(_.asSG).map(_.get),
        hfs = hf.map(_.asHF).map(_.get),
        all = ms,
      )
    }
  }

  implicit class BuffedTuple(ms: (List[SG], List[HF])) {

    def group: Ms = {
      Ms(
        sgs = ms._1,
        hfs = ms._2,
        all = ms._1 ++ ms._2,
      )
    }
  }

  implicit class BuffedNames(names: List[Name]) {
    def stringify: String = names.map(_.name).mkString(",")
  }

  implicit def stringToName(str: String): Name = Name(str)

  implicit def stringToPhotoSetTitleName(str: String): PhotoSetTitle = PhotoSetTitle(str)

  implicit val NameOrdering: Ordering[Name] = new Ordering[Name] {
    override def compare(x: Name, y: Name): Int = x.name.compareTo(y.name)
  }

  implicit val PhotoSetTitleOrdering: Ordering[PhotoSetTitle] = new Ordering[PhotoSetTitle] {
    override def compare(x: PhotoSetTitle, y: PhotoSetTitle): Int = x.name.compareTo(y.name)
  }
}
