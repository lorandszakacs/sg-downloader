package com.lorandszakacs.sg.http

import akka.actor.ActorSystem
import com.lorandszakacs.sg.http.impl.SGClientImpl

import scala.concurrent.ExecutionContext

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
trait SGClientAssembly {

  implicit def actorSystem: ActorSystem

  implicit def executionContext: ExecutionContext

  lazy val suicideGirlsClient: SGClient = SGClientImpl()

}
