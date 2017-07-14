package com.lorandszakacs.sg.model

import com.lorandszakacs.sg.model.impl.{HopefulsDao, IndexDao, SGModelRepositoryImpl, SuicideGirlsDao}
import com.lorandszakacs.util.mongodb.Imports._

import com.lorandszakacs.util.future._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
trait SGModelAssembly {

  def db: Database

  implicit def executionContext: ExecutionContext

  def sgModelRepository: SGModelRepository = _sgModelRepository

  private[model] def nameIndexDao: IndexDao = new IndexDao(db)

  private[model] def suicideGirlsDao: SuicideGirlsDao = new SuicideGirlsDao(db)

  private[model] def hopefulsDao: HopefulsDao = new HopefulsDao(db)

  private[model] lazy val _sgModelRepository = new SGModelRepositoryImpl(
    indexDao = nameIndexDao,
    suicideGirlsDao = suicideGirlsDao,
    hopefulsDao = hopefulsDao
  )

}
