package com.lorandszakacs.sg

import scala.language.implicitConversions

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
package object model {

  implicit class StringBuffedWithModelName(str: String) {
    def toModelName: ModelName = ModelName(str)

    def toTitleName: PhotoSetTitle = PhotoSetTitle(str)
  }

  implicit class BuffedModels(models: List[Model]) {
    def keepSuicideGirls: List[SuicideGirl] = models.map(_.asSuicideGirls) filter (_.isDefined) map (_.get)

    def keepHopefuls: List[Hopeful] = models.map(_.asHopeful) filter (_.isDefined) map (_.get)

    def `SG|Hopeful`: (List[SuicideGirl], List[Hopeful]) = {
      val (sgs, hf) = models partition (_.isSuicideGirl)
      (sgs map (_.asSuicideGirls) map (_.get), hf map (_.asHopeful) map (_.get))
    }
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
