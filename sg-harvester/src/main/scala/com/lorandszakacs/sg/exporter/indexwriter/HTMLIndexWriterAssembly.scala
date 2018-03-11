package com.lorandszakacs.sg.exporter.indexwriter

import com.lorandszakacs.sg.exporter.indexwriter.impl.HTMLIndexWriterImpl

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 17 Jul 2016
  *
  */
private[exporter] trait HTMLIndexWriterAssembly {

  def htmlIndexWriter: HTMLIndexWriterImpl = _htmlIndexWriter

  private lazy val _htmlIndexWriter = new HTMLIndexWriterImpl()

}
