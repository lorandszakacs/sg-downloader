package com.lorandszakacs.sg.exporter.indexwriter

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
  def writeModelIndex(index: ModelsRootIndex)(implicit ws: ExporterSettings): Future[Unit]
}