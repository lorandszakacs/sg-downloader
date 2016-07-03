package com.lorandszakacs.sg.http

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpRequest
import com.lorandszakacs.sg.http.impl.SGClientImpl

import scala.concurrent.ExecutionContext

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
trait SGClientAssembly {

  implicit def actorSystem: ActorSystem

  implicit def executionContext: ExecutionContext

  def authentication: HttpRequest => HttpRequest

  lazy val sgClient: SGClient = SGClientImpl(authentication)

}
