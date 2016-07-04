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

  lazy val sgClientWithNoAuthentication: SGClient = SGClientImpl(identity)

  def sgClientWithAuthentication(authentication: HttpRequest => HttpRequest): SGClient =
    SGClientImpl(authentication)

}
