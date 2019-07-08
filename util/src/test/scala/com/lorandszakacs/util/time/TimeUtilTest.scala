package com.lorandszakacs.util.time

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.Matchers

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 12 Mar 2017
  *
  */
class TimeUtilTest extends AnyFlatSpec with Matchers {

  behavior of "TimeUtil.daysBetween"

  it should "... properly return the inclusive list of days" in {
    val start = LocalDate(2017, 3, 10)
    val end   = LocalDate(2017, 3, 11)

    val result = TimeUtil.daysBetween(start, end)
    (result should have).length(2)
    result should be(List(start, end))
  }

  it should "... properly return the inclusive list of days that spans years" in {
    val start = LocalDate(2017, 11, 27)
    val end   = start.plusDays(120)

    val result = TimeUtil.daysBetween(start, end)
    (result should have).length(121)
    assert(result.head == start)
    assert(result.last == end)
  }

}
