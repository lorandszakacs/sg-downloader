package com.lorandszakacs.sg.reifier

import akka.actor.ActorSystem
import com.lorandszakacs.sg.http.SGClientAssembly
import com.lorandszakacs.sg.reifier.impl.{SGReifierImpl, SessionDaoImpl}
import com.lorandszakacs.util.mongodb.Database

import com.lorandszakacs.util.effects._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 02 Jul 2017
  *
  */
trait ReifierAssembly extends SGClientAssembly {
  def db: Database

  implicit def actorSystem: ActorSystem

  implicit def executionContext: ExecutionContext

  def sgReifier: SGReifier = _sgReifierImpl

  private[reifier] lazy val _sessionDao = new SessionDaoImpl(db)(executionContext)

  private[reifier] lazy val _sgReifierImpl = new SGReifierImpl(sgClient, _sessionDao)
}
