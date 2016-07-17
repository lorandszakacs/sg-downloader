package com.lorandszakacs.sg.exporter.indexwriter

import com.lorandszakacs.sg.exporter.indexwriter.impl.HTMLIndexWriterImpl

import scala.concurrent.ExecutionContext

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 17 Jul 2016
  *
  */
private[exporter] trait HTMLIndexWriterAssembly {

  implicit def executionContext: ExecutionContext

  def htmlIndexWriter: HTMLIndexWriterImpl = _htmlIndexWriter

  private lazy val _htmlIndexWriter = new HTMLIndexWriterImpl()

}
