package com.lorandszakacs.sg.model

import com.lorandszakacs.sg.model.impl.{HopefulsDao, SuicideGirlsDao, NameIndexDao, SGModelRepositoryImpl}
import reactivemongo.api.DefaultDB

import scala.concurrent.ExecutionContext

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
trait SGModelAssembly {

  def db: DefaultDB

  implicit def executionContext: ExecutionContext

  def sgModelRepository: SGModelRepository = _sgModelRepository

  private[model] def nameIndexDao: NameIndexDao = new NameIndexDao(db)

  private[model] def suicideGirlsDao: SuicideGirlsDao = new SuicideGirlsDao(db)

  private[model] def hopefulsDao: HopefulsDao = new HopefulsDao(db)

  private[model] lazy val _sgModelRepository = new SGModelRepositoryImpl(
    nameIndexDao = nameIndexDao,
    suicideGirlsDao = suicideGirlsDao,
    hopefulsDao = hopefulsDao
  )

}
