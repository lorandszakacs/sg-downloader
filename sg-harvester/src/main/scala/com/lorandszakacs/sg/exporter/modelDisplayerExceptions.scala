package com.lorandszakacs.sg.exporter

import com.lorandszakacs.sg.model.ModelName

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 17 Jul 2016
  *
  */
final case class ModelNotFoundException(modelName: ModelName) extends Exception(
  s"Model: ${modelName.name} could not be found."
)