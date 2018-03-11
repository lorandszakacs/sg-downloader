package com.lorandszakacs.sg.http

import akka.actor.ActorSystem
import com.lorandszakacs.sg.http.impl.SGClientImpl

import com.lorandszakacs.util.effects._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
trait SGClientAssembly {

  implicit def actorSystem: ActorSystem

  implicit def scheduler: Scheduler

  lazy val sgClient: SGClient = SGClientImpl()

}
