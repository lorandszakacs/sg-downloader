package com.lorandszakacs.sg.exporter.indexwriter

import java.nio.file.Path

import com.lorandszakacs.sg.exporter.ExporterSettings
import com.lorandszakacs.sg.exporter.html._
import com.lorandszakacs.util.monads.future.FutureUtil._

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 17 Jul 2016
  *
  */
private[exporter] trait HTMLIndexWriter {
  def writeRootModelIndex(index: ModelsRootIndex)(implicit ws: WriterSettings): Future[Unit]

  def rewriteRootIndexFile(indexFile: Html)(implicit ws: WriterSettings): Future[Unit]
}

case class WriterSettings(
  rootFolder: Path,
  rewriteEverything: Boolean
)