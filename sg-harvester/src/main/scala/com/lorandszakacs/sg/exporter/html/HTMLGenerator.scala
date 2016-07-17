package com.lorandszakacs.sg.exporter.html

import com.lorandszakacs.sg.model._
import com.lorandszakacs.util.monads.future.FutureUtil._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 17 Jul 2016
  *
  */
private[exporter] trait HTMLGenerator {
  def createHTMLPageForModels(models: List[Model])(implicit settings: HtmlSettings): Future[ModelsRootIndex]
}

case class HtmlSettings(
  indexFileName: String = "index.html",
  rootIndexTitle: String
)

case class Html(
  value: String
)

case class ModelsRootIndex(
  indexFileName: String,
  html: Html,
  models: List[ModelIndex]
)

case class ModelIndex(
  name: ModelName,
  modelIndexHtml: Html,
  modelIndexHtmlFileName: String,
  photoSets: List[PhotoSetIndex]
)

case class PhotoSetIndex(
  html: Html,
  htmlFileName: String,
  displayName: String
)
