package com.lorandszakacs.sg

import scala.language.implicitConversions

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
package object model {

  implicit class StringBuffedWithModelName(str: String) {
    def toModelName: ModelName = ModelName(str)

    def toTitleName: PhotoSetTitle = PhotoSetTitle(str)
  }

  implicit def stringToModelName(str: String): ModelName = ModelName(str)

  implicit def stringToPhotoSetTitleName(str: String): PhotoSetTitle = PhotoSetTitle(str)

  implicit val ModelNameOrdering: Ordering[ModelName] = new Ordering[ModelName] {
    override def compare(x: ModelName, y: ModelName): Int = x.name.compareTo(y.name)
  }

  implicit val PhotoSetTitleOrdering: Ordering[PhotoSetTitle] = new Ordering[PhotoSetTitle] {
    override def compare(x: PhotoSetTitle, y: PhotoSetTitle): Int = x.name.compareTo(y.name)
  }
}
