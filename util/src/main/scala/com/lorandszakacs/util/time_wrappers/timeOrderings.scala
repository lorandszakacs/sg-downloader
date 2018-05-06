package com.lorandszakacs.util.time_wrappers

import java.{time => jt}

/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 06 May 2018
  *
  */
object LocalDateOrdering extends Ordering[jt.LocalDate] {
  override def compare(x: jt.LocalDate, y: jt.LocalDate): Int = x.compareTo(y)
}

object InstantOrdering extends Ordering[jt.Instant] {
  override def compare(x: jt.Instant, y: jt.Instant): Int = x.compareTo(y)
}
