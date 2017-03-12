package com.lorandszakacs.sg.exporter.html

import java.nio.file.Path

import com.lorandszakacs.sg.model._
import com.lorandszakacs.util.monads.future.FutureUtil._
import org.joda.time.LocalDate

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 17 Jul 2016
  *
  */
private[exporter] trait HTMLGenerator {
  def createHTMLPageForModels(models: List[Model])(implicit settings: HtmlSettings): Future[ModelsRootIndex]

  def createRootIndex(models: List[ModelName])(implicit settings: HtmlSettings): Future[Html]

  def createNewestPage(models: List[(LocalDate, List[Model])]): Future[Html]
}

case class HtmlSettings(
  indexFileName: String = "index.html",
  rootIndexTitle: String
)

case class Html(
  relativePathAndName: String,
  content: String
)

case class ModelsRootIndex(
  html: Html,
  models: List[ModelIndex]
)

case class ModelIndex(
  name: ModelName,
  html: Html,
  photoSets: List[PhotoSetIndex]
)

case class PhotoSetIndex(
  html: Html,
  displayName: String
)
