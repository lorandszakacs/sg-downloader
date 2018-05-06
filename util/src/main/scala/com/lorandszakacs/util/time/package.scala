package com.lorandszakacs.util

import java.{time => jt}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 14 Jul 2017
  *
  */
package object time {
  type Month    = jt.Month
  type MonthDay = jt.MonthDay
  type Year     = jt.Year

  type LocalDate     = jt.LocalDate
  type LocalDateTime = jt.LocalDateTime
  type Instant       = jt.Instant

  type DateTimeFormatter = jt.format.DateTimeFormatter

  val Year: time_wrappers.StaticYear = time_wrappers.StaticYear

  val LocalDate:     time_wrappers.StaticLocalDate.type     = time_wrappers.StaticLocalDate
  val LocalDateTime: time_wrappers.StaticLocalDateTime.type = time_wrappers.StaticLocalDateTime
  val Instant:       time_wrappers.StaticInstant.type       = time_wrappers.StaticInstant

  val DateTimeFormatter: time_wrappers.StaticDateTimeFormatter.type = time_wrappers.StaticDateTimeFormatter

  implicit val JavaTimeInstantOrdering:   Ordering[Instant]   = time_wrappers.InstantOrdering
  implicit val JavaTimeLocalDateOrdering: Ordering[LocalDate] = time_wrappers.LocalDateOrdering
}
