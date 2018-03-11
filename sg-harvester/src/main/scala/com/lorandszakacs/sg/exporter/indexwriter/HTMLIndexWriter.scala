package com.lorandszakacs.sg.exporter.indexwriter

import java.nio.file.Path

import com.lorandszakacs.sg.exporter.html._
import com.lorandszakacs.util.effects._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 17 Jul 2016
  *
  */
private[exporter] trait HTMLIndexWriter {
  def writeRootMIndex(index: MRootIndex)(implicit ws: WriterSettings): IO[Unit]

  def rewriteRootIndexFile(indexFile: Html)(implicit ws: WriterSettings): IO[Unit]

  def rewriteNewestMPage(indexFile: Html)(implicit ws: WriterSettings): IO[Unit]
}

case class WriterSettings(
  rootFolder:        Path,
  rewriteEverything: Boolean
)
