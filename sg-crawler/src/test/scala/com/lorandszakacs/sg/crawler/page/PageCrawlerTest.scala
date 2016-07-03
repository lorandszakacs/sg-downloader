package com.lorandszakacs.sg.crawler.page

import akka.actor.ActorSystem
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Span}
import org.scalatest.{Matchers, fixture}

import scala.concurrent.ExecutionContext

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 03 Jul 2016
  *
  */
trait PageCrawlerTest extends fixture.FlatSpec with ScalaFutures with Matchers {
  implicit val actorSystem: ActorSystem = ActorSystem(s"${super.getClass.getSimpleName}")
  implicit val ec: ExecutionContext = actorSystem.dispatcher

  implicit override def patienceConfig: PatienceConfig = PatienceConfig(
    timeout = scaled(Span(10000, Millis)),
    interval = scaled(Span(100, Millis)))
}
