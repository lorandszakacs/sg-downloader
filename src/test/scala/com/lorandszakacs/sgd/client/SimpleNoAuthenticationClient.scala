package com.lorandszakacs.sgd.client

import scala.concurrent.ExecutionContext

import com.lorandszakacs.sgd.http.{AuthenticationInfo, NoAuthenticationInfo}

import akka.actor.ActorSystem

class SimpleNoAuthenticationClient(implicit val actorSystem: ActorSystem, val executionContext: ExecutionContext) extends com.lorandszakacs.sgd.http.Client {
  def authentication: AuthenticationInfo = new NoAuthenticationInfo
}