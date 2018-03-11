package com.lorandszakacs.sg.exporter.html

import com.lorandszakacs.sg.exporter.html.impl.HTMLGeneratorImpl

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 17 Jul 2016
  *
  */
private[exporter] trait HTMLGeneratorAssembly {

  private[exporter] def htmlGenerator: HTMLGenerator = _htmlGenerator

  private lazy val _htmlGenerator = new HTMLGeneratorImpl()

}
