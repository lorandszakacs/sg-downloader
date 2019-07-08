package com.lorandszakacs.sg.http

import com.lorandszakacs.util.effects._
import com.lorandszakacs.util.logger._

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
object PatienceConfig {
  val defaultDuration: FiniteDuration = 100 millis
  val doubleDefault:   FiniteDuration = defaultDuration.mul(2)
}

final case class PatienceConfig(
  timer:    Timer[IO],
  throttle: FiniteDuration = PatienceConfig.defaultDuration,
) {
  implicit private val logger: Logger[IO] = Logger.getLogger[IO]

  def throttleThread: IO[Unit] = throttleAmount(throttle)

  def halfThrottle: IO[Unit] = throttleAmount(throttle.div(2))

  def quarterThrottle: IO[Unit] = throttleAmount(throttle.div(4))

  private def throttleAmount(duration: FiniteDuration): IO[Unit] = {
    logger.info(s"waiting: ${duration.toString}") *> timer.sleep(throttle)
  }

  def throttleAfter[T](t: IO[T]): IO[T] = t <* this.throttleThread

  def throttleHalfAfter[T](t: IO[T]): IO[T] = t <* this.halfThrottle

  def throttleQuarterAfter[T](t: IO[T]): IO[T] = t <* this.quarterThrottle

}
