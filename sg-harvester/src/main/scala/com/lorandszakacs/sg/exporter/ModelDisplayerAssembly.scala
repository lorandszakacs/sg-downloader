package com.lorandszakacs.sg.exporter

import com.lorandszakacs.sg.exporter.html.HTMLGeneratorAssembly
import com.lorandszakacs.sg.exporter.impl.SGExporterImpl
import com.lorandszakacs.sg.exporter.indexwriter.HTMLIndexWriterAssembly
import com.lorandszakacs.sg.model.SGModelAssembly

import scala.concurrent.ExecutionContext

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 17 Jul 2016
  *
  */
trait ModelDisplayerAssembly extends HTMLIndexWriterAssembly with HTMLGeneratorAssembly {
  this: SGModelAssembly =>

  implicit def executionContext: ExecutionContext

  def modelDisplayer: SGExporter = _displayer

  private lazy val _displayer = new SGExporterImpl(
    repo = sgModelRepository,
    html = htmlGenerator,
    fileWriter = htmlIndexWriter
  )(executionContext)

}
