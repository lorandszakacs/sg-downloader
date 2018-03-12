package com.lorandszakacs.util.time

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
  def daysBetween(start: LocalDate, end: LocalDate): List[LocalDate] = {
    val nrOfDays = Days.daysBetween(start, end).getDays
    if (start > end) {
      throw new IllegalArgumentException(
        s".... days between cannot be computed for start($start) smaller than end($end)"
      )
    }
    (0 to nrOfDays map { d =>
      start.plusDays(d)
    }).toList
  }

}
