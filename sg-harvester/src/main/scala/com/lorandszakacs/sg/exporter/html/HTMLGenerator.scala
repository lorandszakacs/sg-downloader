package com.lorandszakacs.sg.exporter.html

import com.lorandszakacs.sg.model._
import com.lorandszakacs.util.effects._
import com.lorandszakacs.util.time._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 17 Jul 2016
  *
  */
private[exporter] trait HTMLGenerator {
  def createHTMLPageForMs(ms: List[M])(implicit settings: HtmlSettings): IO[MRootIndex]

  def createRootIndex(names: List[Name])(implicit settings: HtmlSettings): IO[Html]

  def createNewestPage(ms: List[(LocalDate, List[M])], favorites: Set[Name]): IO[Html]
}

case class HtmlSettings(
  indexFileName:  String,
  rootIndexTitle: String,
)

case class Html(
  relativePathAndName: String,
  content:             String,
)

case class MRootIndex(
  html: Html,
  ms:   List[MIndex],
)

case class MIndex(
  name:      Name,
  html:      Html,
  photoSets: List[PhotoSetIndex],
)

case class PhotoSetIndex(
  html:        Html,
  displayName: String,
)
