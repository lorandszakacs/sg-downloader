package com.lorandszakacs.util.time_wrappers

import java.time.LocalDate
import java.{time => jt}

import cats.effect.Sync

/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 06 May 2018
  *
  */
object StaticLocalDate extends StaticLocalDate

trait StaticLocalDate {
  @inline def apply(year:     Int, month: Int, day: Int): jt.LocalDate = jt.LocalDate.of(year, month, day)
  @inline def apply(year:     Int, month: jt.Month, day: Int): jt.LocalDate = jt.LocalDate.of(year, month, day)
  @inline def apply(temporal: jt.temporal.TemporalAccessor): jt.LocalDate = jt.LocalDate.from(temporal)

  @inline def of(year:     Int, month: Int, day: Int): jt.LocalDate = jt.LocalDate.of(year, month, day)
  @inline def of(year:     Int, month: jt.Month, day: Int): jt.LocalDate = jt.LocalDate.of(year, month, day)
  @inline def of(temporal: jt.temporal.TemporalAccessor): jt.LocalDate = jt.LocalDate.from(temporal)

  @inline def from(temporal: jt.temporal.TemporalAccessor): jt.LocalDate = jt.LocalDate.from(temporal)

  @inline def unsafeNow(): jt.LocalDate = jt.LocalDate.now()
  @inline def unsafeNow(clock:  java.time.Clock):  jt.LocalDate = jt.LocalDate.now(clock)
  @inline def unsafeNow(zoneId: java.time.ZoneId): jt.LocalDate = jt.LocalDate.now(zoneId)

  @inline def now[F[_]](implicit sf: Sync[F]): F[jt.LocalDate] = sf.delay(unsafeNow())

  @inline def now[F[_]](clock:  java.time.Clock)(implicit sf:  Sync[F]): F[jt.LocalDate] = sf.delay(unsafeNow(clock))
  @inline def now[F[_]](zoneId: java.time.ZoneId)(implicit sf: Sync[F]): F[jt.LocalDate] = sf.delay(unsafeNow(zoneId))

  @inline def min: jt.LocalDate = jt.LocalDate.MIN
  @inline def max: jt.LocalDate = jt.LocalDate.MAX

  @inline def unsafeYesterday(): jt.LocalDate = jt.LocalDate.now().minusDays(1)
  @inline def unsafeToday():     jt.LocalDate = jt.LocalDate.now()

  @inline def yesterday[F[_]](implicit sf: Sync[F]): F[jt.LocalDate] = sf.delay(unsafeYesterday())
  @inline def today[F[_]](implicit sf:     Sync[F]): F[jt.LocalDate] = sf.delay(unsafeToday())

  @inline def parse(s: String): LocalDate = jt.LocalDate.parse(s)

}
