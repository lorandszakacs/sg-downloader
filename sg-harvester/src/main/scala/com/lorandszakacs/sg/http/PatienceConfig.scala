package com.lorandszakacs.sg.http

import com.lorandszakacs.util.effects._
import org.iolog4s.Logger

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
object PatienceConfig {
  val defaultDuration: FiniteDuration = 200 millis
  val doubleDefault:   FiniteDuration = defaultDuration.mul(2)
}

final case class PatienceConfig(
  throttle: FiniteDuration = PatienceConfig.defaultDuration
) {
  private implicit val logger: Logger[Task] = Logger.create[Task]

  def throttleThread: Task[Unit] = throttleAmount(throttle)

  def halfThrottle: Task[Unit] = throttleAmount(throttle.div(2))

  def quarterThrottle: Task[Unit] = throttleAmount(throttle.div(4))

  private def throttleAmount(duration: FiniteDuration): Task[Unit] = {
    logger.info(s"waiting: ${duration.toString}") >>
      Task(Thread.sleep(throttle.toMillis))
  }

  def throttleAfter[T](t: Task[T]): Task[T] = t <* this.throttleThread

  def throttleHalfAfter[T](t: Task[T]): Task[T] = t <* this.halfThrottle

  def throttleQuarterAfter[T](t: Task[T]): Task[T] = t <* this.quarterThrottle

}
