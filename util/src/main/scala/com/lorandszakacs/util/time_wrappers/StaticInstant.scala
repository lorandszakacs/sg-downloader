package com.lorandszakacs.util.time_wrappers

import java.{time => jt}

import cats.effect.Sync

object StaticInstant extends StaticInstant

trait StaticInstant {

  @inline def unsafeNow(): jt.Instant = jt.Instant.now()
  @inline def unsafeNow(clock: java.time.Clock): jt.Instant = jt.Instant.now(clock)

  @inline def now[F[_]](implicit sf: Sync[F]): F[jt.Instant] = sf.delay(unsafeNow())

  @inline def now[F[_]](clock: java.time.Clock)(implicit sf: Sync[F]): F[jt.Instant] = sf.delay(unsafeNow(clock))

}
