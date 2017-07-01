package com.lorandszakacs.sg.exporter

import com.lorandszakacs.sg.exporter.html.HTMLGeneratorAssembly
import com.lorandszakacs.sg.exporter.impl.SGExporterImpl
import com.lorandszakacs.sg.exporter.indexwriter.HTMLIndexWriterAssembly
import com.lorandszakacs.sg.model.SGModelAssembly

import com.lorandszakacs.util.future._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 17 Jul 2016
  *
  */
trait ModelExporterAssembly extends HTMLIndexWriterAssembly with HTMLGeneratorAssembly {
  this: SGModelAssembly =>

  implicit def executionContext: ExecutionContext

  def sgExporter: SGExporter = _displayer

  private lazy val _displayer = new SGExporterImpl(
    repo = sgModelRepository,
    html = htmlGenerator,
    fileWriter = htmlIndexWriter
  )(executionContext)

}
