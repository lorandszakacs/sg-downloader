package com.lorandszakacs.util.time

import org.scalatest.{FlatSpec, Matchers}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 12 Mar 2017
  *
  */
class TimeUtilTest extends FlatSpec with Matchers {

  behavior of "TimeUtil.daysBetween"

  it should "... properly return the inclusive list of days" in {
    val start = new LocalDate(2017, 3, 10)
    val end = new LocalDate(2017, 3, 11)
    val result = TimeUtil.daysBetween(start, end)
    result should have length 2
    result should be(List(start, end))
  }

}
