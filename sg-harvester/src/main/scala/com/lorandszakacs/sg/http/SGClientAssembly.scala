package com.lorandszakacs.sg.http

import akka.actor.ActorSystem
import com.lorandszakacs.sg.http.impl.SGClientImpl

import com.lorandszakacs.util.future._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
trait SGClientAssembly {

  implicit def actorSystem: ActorSystem

  implicit def executionContext: ExecutionContext

  lazy val sgClient: SGClient = SGClientImpl()

}
