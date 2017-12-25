package com.lorandszakacs.sg.exporter

import com.lorandszakacs.sg.model.Name

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 17 Jul 2016
  *
  */
final case class ModelNotFoundException(modelName: Name)
    extends Exception(
      s"Model: ${modelName.name} could not be found."
    )
