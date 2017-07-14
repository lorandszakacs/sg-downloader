package com.lorandszakacs.util

import com.github.nscala_time.time.Imports
import com.lorandszakacs.util.time_wrappers.StaticDays
import org.joda

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 14 Jul 2017
  *
  */
package object time extends Imports {
  type Days = joda.time.Days
  val Days: StaticDays.type = StaticDays

}
