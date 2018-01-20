package com.lorandszakacs.util.future

import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 11 Apr 2016
  *
  */
class FutureSerializeTest extends FlatSpec with Matchers {

  implicit val ec: ExecutionContext = ExecutionContext.global

  behavior of "Future.serialize"

  //===========================================================================
  //===========================================================================

  it should "... make sure that the futures in the list are not executed in parallel" in {
    val input: Seq[Int] = List(1, 2, 3, 4, 5, 6)
    val expected = Seq("1", "2", "3", "4", "5", "6")

    var previouslyProcessed: Option[Int] = None
    var startedFlag:         Option[Int] = None

    val eventualResult: Future[Seq[String]] = Future.serialize(input) { i =>
      val toWaitInMillis: Long = (Math.random() * 1000).toLong % 200
      Future {
        assert(
          startedFlag.isEmpty,
          s"started flag should have been empty at the start of each future but was: $startedFlag"
        )
        previouslyProcessed foreach { previous =>
          assertResult(expected = i - 1, "... the futures were not executed in the correct order.")(actual = previous)
        }
        startedFlag = Some(i)
        Thread.sleep(toWaitInMillis)
        startedFlag         = None
        previouslyProcessed = Some(i)
        i.toString
      }
    }

    val result = Await.result(eventualResult, 1 minute)
    assertResult(expected = expected, "unequal lists")(actual = result)
  }

  //===========================================================================
  //===========================================================================

  it should "... work on empty list just as fine" in {
    val input: Seq[Int] = List()
    val expected = Seq()

    var previouslyProcessed: Option[Int] = None
    var startedFlag:         Option[Int] = None

    val eventualResult: Future[Seq[String]] = Future.serialize(input) { i =>
      val toWaitInMillis: Long = (Math.random() * 1000).toLong % 200
      Future {
        assert(
          startedFlag.isEmpty,
          s"started flag should have been empty at the start of each future but was: $startedFlag"
        )
        previouslyProcessed foreach { previous =>
          assertResult(expected = i - 1, "... the futures were not executed in the correct order.")(actual = previous)
        }
        startedFlag = Some(i)
        Thread.sleep(toWaitInMillis)
        startedFlag         = None
        previouslyProcessed = Some(i)
        i.toString
      }
    }

    val result = Await.result(eventualResult, 1 minute)
    assertResult(expected = expected, "unequal lists")(actual = result)
  }

  //===========================================================================
  //===========================================================================

  it should "... work on sets" in {
    val input:    Set[Int]    = Set(1, 2, 3, 4)
    val expected: Set[String] = Set("1", "2", "3", "4")

    var startedFlag: Option[Int] = None

    val eventualResult: Future[Set[String]] = Future.serialize(input) { i =>
      val toWaitInMillis: Long = (Math.random() * 1000).toLong % 200
      Future {
        assert(
          startedFlag.isEmpty,
          s"started flag should have been empty at the start of each future but was: $startedFlag"
        )
        startedFlag = Some(i)
        Thread.sleep(toWaitInMillis)
        startedFlag = None
        i.toString
      }
    }

    val result = Await.result(eventualResult, 1 minute)
    assertResult(expected = expected, "unequal lists")(actual = result)
  }

}
