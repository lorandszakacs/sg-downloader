package com.lorandszakacs.sg.reifier

import com.lorandszakacs.sg.http.SGClientAssembly
import com.lorandszakacs.sg.reifier.impl.{SGReifierImpl, SessionDaoImpl}
import com.lorandszakacs.util.mongodb.Database
import com.lorandszakacs.util.effects._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 02 Jul 2017
  *
  */
trait ReifierAssembly extends SGClientAssembly {
  def db: Database

  implicit def dbIOScheduler: DBIOScheduler

  def sgReifier: SGReifier = _sgReifierImpl

  private[reifier] lazy val _sessionDao = new SessionDaoImpl(db)(dbIOScheduler)

  private[reifier] lazy val _sgReifierImpl = new SGReifierImpl(sgClient, _sessionDao)

  def initReifierAssembly: Task[Unit] = _sessionDao.init
}
