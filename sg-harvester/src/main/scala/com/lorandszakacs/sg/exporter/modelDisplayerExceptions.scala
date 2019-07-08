package com.lorandszakacs.sg.exporter

import com.lorandszakacs.sg.model.Name

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 17 Jul 2016
  *
  */
final case class NameNotFoundException(name: Name)
    extends Exception(
      s"M: ${name.name} could not be found.",
    )
