package com.lorandszakacs.util.time

import java.{time => jt}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 14 Jul 2017
  *
  */
object TimeUtil {

  /**
    * Returns a list containing all the dates between the start, and given end dates,
    * inclusively.
    */
  def daysBetween(start: jt.LocalDate, end: jt.LocalDate): List[jt.LocalDate] = {
    val nrOfDays = jt.Period.between(start, end).getDays
    if (start.isAfter(end)) {
      throw new IllegalArgumentException(
        s".... days between cannot be computed for start($start) smaller than end($end)"
      )
    }
    (0 to nrOfDays map { d =>
      start.plusDays(d.toLong)
    }).toList
  }

  /**
    * The default format
    */
  val localDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd")

}
