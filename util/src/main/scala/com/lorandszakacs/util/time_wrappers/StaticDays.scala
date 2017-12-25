package com.lorandszakacs.util.time_wrappers

import org.joda.time.{Days, ReadablePartial}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 14 Jul 2017
  *
  */
object StaticDays extends StaticDays

trait StaticDays {

  def daysBetween(start: ReadablePartial, end: ReadablePartial): Days = {
    org.joda.time.Days.daysBetween(start, end)
  }

}
