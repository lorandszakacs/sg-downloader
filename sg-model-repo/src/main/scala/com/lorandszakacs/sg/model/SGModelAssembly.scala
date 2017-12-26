package com.lorandszakacs.sg.model

import com.lorandszakacs.sg.model.impl.SGAndHFRepositoryImpl
import com.lorandszakacs.util.future._
import com.lorandszakacs.util.mongodb.Database

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
trait SGModelAssembly {

  def db: Database

  implicit def executionContext: ExecutionContext

  def sgAndHFRepository: SGAndHFRepository = _sgAndHFRepository

  private[model] lazy val _sgAndHFRepository = new SGAndHFRepositoryImpl(db)

}
