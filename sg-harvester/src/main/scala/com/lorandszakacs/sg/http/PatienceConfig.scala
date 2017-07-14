package com.lorandszakacs.sg.http

import com.lorandszakacs.util.future._
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
) {
  def throttleThread(): Unit = Thread.sleep(throttle.toMillis)

  def halfThrottle(): Unit = Thread.sleep(throttle.toMillis / 2)

  def quarterThrottle(): Unit = Thread.sleep(throttle.toMillis / 4)

  def throttleAfter[T](thunk: => Future[T])(implicit ec: ExecutionContext): Future[T] = {
    val f = thunk
    f.map { r =>
      this.throttleThread()
      r
    }
  }

}
