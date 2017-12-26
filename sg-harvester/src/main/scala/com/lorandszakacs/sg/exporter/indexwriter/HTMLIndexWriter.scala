package com.lorandszakacs.sg.exporter.indexwriter

import java.nio.file.Path

import com.lorandszakacs.sg.exporter.html._
import com.lorandszakacs.util.future._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 17 Jul 2016
  *
  */
private[exporter] trait HTMLIndexWriter {
  def writeRootMIndex(index: MRootIndex)(implicit ws: WriterSettings): Future[Unit]

  def rewriteRootIndexFile(indexFile: Html)(implicit ws: WriterSettings): Future[Unit]

  def rewriteNewestModelPage(indexFile: Html)(implicit ws: WriterSettings): Future[Unit]
}

case class WriterSettings(
  rootFolder:        Path,
  rewriteEverything: Boolean
)
