package com.lorandszakacs.util.time_wrappers

import java.{time => jt}

import cats.effect.Sync

/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 06 May 2018
  *
  */
object StaticYear extends StaticYear

trait StaticYear {
  def unsafeNow(): jt.Year = jt.Year.now()
  def now[F[_]](implicit sf: Sync[F]): F[jt.Year] = sf.delay(unsafeNow())
}
