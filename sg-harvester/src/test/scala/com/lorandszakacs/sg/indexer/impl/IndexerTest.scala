package com.lorandszakacs.sg.indexer.impl

import com.lorandszakacs.sg.http
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time._
import org.scalatest._
import com.lorandszakacs.util.effects._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.FixtureAnyFlatSpec

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 03 Jul 2016
  *
  */
trait IndexerTest extends FixtureAnyFlatSpec with ScalaFutures with Matchers with BeforeAndAfterAll {
  implicit protected val (ec: ExecutionContextMainFT, cs: ContextShift[IO], timer: Timer[IO]) =
    IORuntime.defaultMainRuntimeWithEC("sg-client-test").value

  implicit val crawlerPatienceConfig: http.PatienceConfig =
    http.PatienceConfig(timer, http.PatienceConfig.doubleDefault)

  implicit val httpIOSch: HTTPIOScheduler = HTTPIOScheduler(ec)

  implicit override def patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(Span(10000, Millis)), interval = scaled(Span(100, Millis)))
}
