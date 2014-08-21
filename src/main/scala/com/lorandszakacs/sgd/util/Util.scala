package com.lorandszakacs.sgd.util

import java.time.LocalDate

import scala.util.{ Failure, Success, Try }

object Util {
  def parseStringToLocalDate(t: String): Try[LocalDate] = {
    val time = t.trim()
    try {
      //Aug 1, 2012
      val datePattern = """(\w\w\w) (\d*), (\d\d\d\d)""".r
      val datePattern(month, day, year) = time

      val months = Map(1 -> "Jan", 2 -> "Feb", 3 -> "Mar",
        4 -> "Apr", 5 -> "May", 6 -> "Jun",
        7 -> "Jul", 8 -> "Aug", 9 -> "Sep",
        10 -> "Oct", 11 -> "Nov", 12 -> "Dec").map(p => p._2 -> p._1)
      val monthAsInt = months("Aug")

      val localDate = LocalDate.of(year.toInt, monthAsInt, day.toInt)
      Success(localDate)
    } catch {
      case e: Throwable => Failure(e)
    }
  }
}