package com.lorandszakacs.sg.reifier

import com.lorandszakacs.sg.http.{SGClient, SGClientAssembly}
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

  implicit def futureLift: FutureLift[IO]

  def sgReifier(sgClient: SGClient): SGReifier = new SGReifierImpl(sgClient, _sessionDao)

  private[reifier] lazy val _sessionDao = new SessionDaoImpl(db)(dbIOScheduler, futureLift)

  def initReifierAssembly: IO[Unit] = _sessionDao.init
}
