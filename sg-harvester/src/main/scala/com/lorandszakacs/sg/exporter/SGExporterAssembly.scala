package com.lorandszakacs.sg.exporter

import com.lorandszakacs.sg.exporter.html.HTMLGeneratorAssembly
import com.lorandszakacs.sg.exporter.impl.SGExporterImpl
import com.lorandszakacs.sg.exporter.indexwriter.HTMLIndexWriterAssembly
import com.lorandszakacs.sg.model.SGRepoAssembly

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 17 Jul 2016
  *
  */
trait SGExporterAssembly extends HTMLIndexWriterAssembly with HTMLGeneratorAssembly {
  this: SGRepoAssembly =>

  def sgExporter: SGExporter = _exporter

  private lazy val _exporter = new SGExporterImpl(
    repo       = sgAndHFRepository,
    html       = htmlGenerator,
    fileWriter = htmlIndexWriter,
  )

}
