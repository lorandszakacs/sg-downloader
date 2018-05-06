package com.lorandszakacs.util.time_wrappers

import java.{time => jt}

import cats.effect.Sync

/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 06 May 2018
  *
  */
object StaticLocalDateTime extends StaticLocalDateTime

trait StaticLocalDateTime {

  @inline def apply(year: Int, month: Int, day: Int, hour: Int, minute: Int): jt.LocalDateTime =
    jt.LocalDateTime.of(year, month, day, hour, minute)

  @inline def apply(year: Int, month: jt.Month, day: Int, hour: Int, minute: Int): jt.LocalDateTime =
    jt.LocalDateTime.of(year, month, day, hour, minute)

  @inline def of(year: Int, month: Int, day: Int, hour: Int, minute: Int): jt.LocalDateTime =
    jt.LocalDateTime.of(year, month, day, hour, minute)

  @inline def of(year: Int, month: jt.Month, day: Int, hour: Int, minute: Int): jt.LocalDateTime =
    jt.LocalDateTime.of(year, month, day, hour, minute)

  @inline def unsafeNow(): jt.LocalDateTime = jt.LocalDateTime.now()
  @inline def unsafeNow(clock:  java.time.Clock):  jt.LocalDateTime = jt.LocalDateTime.now(clock)
  @inline def unsafeNow(zoneId: java.time.ZoneId): jt.LocalDateTime = jt.LocalDateTime.now(zoneId)

  @inline def now[F[_]](implicit sf: Sync[F]): F[jt.LocalDateTime] = sf.delay(unsafeNow())

  @inline def now[F[_]](clock: java.time.Clock)(implicit sf: Sync[F]): F[jt.LocalDateTime] = sf.delay(unsafeNow(clock))

  @inline def now[F[_]](zoneId: java.time.ZoneId)(implicit sf: Sync[F]): F[jt.LocalDateTime] =
    sf.delay(unsafeNow(zoneId))

  @inline def min: jt.LocalDateTime = jt.LocalDateTime.MIN
  @inline def max: jt.LocalDateTime = jt.LocalDateTime.MAX

}
