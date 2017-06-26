package com.lorandszakacs.sg.crawler.impl

import akka.actor.ActorSystem
import com.lorandszakacs.sg.http
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Span}
import org.scalatest.{Matchers, fixture}

import com.lorandszakacs.util.future._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 03 Jul 2016
  *
  */
trait PageCrawlerTest extends fixture.FlatSpec with ScalaFutures with Matchers {
  implicit val actorSystem: ActorSystem = ActorSystem(s"${super.getClass.getSimpleName}")
  implicit val ec: ExecutionContext = actorSystem.dispatcher
  implicit val crawlerPatienceConfig: http.PatienceConfig = http.PatienceConfig()

  implicit override def patienceConfig: PatienceConfig = PatienceConfig(
    timeout = scaled(Span(10000, Millis)),
    interval = scaled(Span(100, Millis)))
}
