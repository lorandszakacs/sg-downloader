package com.lorandszakacs.sg.http

import com.lorandszakacs.util.future._
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
final case class PatienceConfig(
  throttle: FiniteDuration = 200 millis
) extends StrictLogging {

  def throttleThread(): Unit = throttleAmount(throttle)

  def halfThrottle(): Unit = throttleAmount(throttle.div(2))

  def quarterThrottle(): Unit = throttleAmount(throttle.div(4))

  private def throttleAmount(duration: FiniteDuration): Unit = {
    logger.info(s"waiting: ${duration.toString}")
    Thread.sleep(throttle.toMillis)
  }

  def throttleAfter[T](thunk: => Future[T])(implicit ec: ExecutionContext): Future[T] = {
    val f = thunk
    f.map { r =>
      this.throttleThread()
      r
    }
  }

  def throttleHalfAfter[T](thunk: => Future[T])(implicit ec: ExecutionContext): Future[T] = {
    val f = thunk
    f.map { r =>
      this.halfThrottle()
      r
    }
  }

  def throttleQuarterAfter[T](thunk: => Future[T])(implicit ec: ExecutionContext): Future[T] = {
    val f = thunk
    f.map { r =>
      this.quarterThrottle()
      r
    }
  }

}
