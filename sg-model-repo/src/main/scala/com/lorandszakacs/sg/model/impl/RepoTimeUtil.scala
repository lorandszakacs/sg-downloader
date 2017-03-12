package com.lorandszakacs.sg.model.impl

import com.github.nscala_time.time.Imports._
import org.joda.time.Days


/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 12 Mar 2017
  *
  */
object RepoTimeUtil {

  /**
    * Returns a list containing all the dates between the start, and given end dates,
    * inclusively.
    */
  def daysBetween(start: LocalDate, end: LocalDate): List[LocalDate] = {
    val nrOfDays = Days.daysBetween(start, end).getDays
    if (start > end) {
      throw new IllegalArgumentException(s".... days between cannot be computed for start($start) smaller than end($end)")
    }
    (0 to nrOfDays map { d => start.plusDays(d) }).toList
  }
}
