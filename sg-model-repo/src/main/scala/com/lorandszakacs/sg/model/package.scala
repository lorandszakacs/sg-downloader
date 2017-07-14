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

  implicit val modelIdentity: Identity[Model] = Identity[Model] { (m1, m2) =>
    m1.name == m2.name
  }

  implicit val suicideGirlIdentifier: Identifier[SuicideGirl, ModelName] = Identifier[SuicideGirl, ModelName] { m: SuicideGirl => m.name }
  implicit val hopefulIdentifier: Identifier[Hopeful, ModelName] = Identifier[Hopeful, ModelName] { m: Hopeful => m.name }

  implicit class StringBuffedWithModelName(str: String) {
    def toModelName: ModelName = ModelName(str)

    def toTitleName: PhotoSetTitle = PhotoSetTitle(str)
  }

  implicit class BuffedModels(models: List[Model]) {
    def group: Models = {
      val (sgs, hf) = models partition (_.isSuicideGirl)
      Models(
        sgs = sgs map (_.asSuicideGirl) map (_.get),
        hfs = hf map (_.asHopeful) map (_.get),
        all = models
      )
    }
  }

  implicit class BuffedTuple(models: (List[SuicideGirl], List[Hopeful])) {
    def group: Models = {
      Models(
        sgs = models._1,
        hfs = models._2,
        all = models._1 ++ models._2
      )
    }
  }

  implicit class BuffedModelNames(models: List[ModelName]) {
    def stringify: String = models.map(_.name).mkString(",")
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
