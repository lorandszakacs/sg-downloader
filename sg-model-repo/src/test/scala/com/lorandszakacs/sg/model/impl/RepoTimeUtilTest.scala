package com.lorandszakacs.sg.model.impl

import com.github.nscala_time.time.Imports._
import org.scalatest.{FlatSpec, Matchers}

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 12 Mar 2017
  *
  */
class RepoTimeUtilTest extends FlatSpec with Matchers {

  behavior of "RepoTimeUtil.daysBetween"

  it should "... properly return the inclusive list of days" in {
    val start = new LocalDate(2017, 3, 10)
    val end = new LocalDate(2017, 3, 11)
    val result = RepoTimeUtil.daysBetween(start, end)
    result should have length 2
    result should be(List(start, end))
  }

}
