package com.lorandszakacs.sg.model

import com.lorandszakacs.sg.model.impl.SGAndHFRepositoryImpl
import com.lorandszakacs.util.effects._
import com.lorandszakacs.util.mongodb.Database

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
trait SGRepoAssembly {

  def db: Database

  implicit def scheduler: Scheduler

  def sgAndHFRepository: SGAndHFRepository = _sgAndHFRepository

  private[model] lazy val _sgAndHFRepository = new SGAndHFRepositoryImpl(db)

}
