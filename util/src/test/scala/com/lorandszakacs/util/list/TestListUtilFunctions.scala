package com.lorandszakacs.util.list

import ListUtilFunctions._

import org.scalatest.FlatSpec
import com.lorandszakacs.util.math._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 12 Jul 2017
  *
  */
class TestListUtilFunctions extends FlatSpec {

  private implicit val indentifieable: Identity[I] = Identity[I] { (i1, i2) =>
    i1.id == i2.id
  }

  behavior of "list.replace"

  it should "... replace the elements that identify the same" in {
    val thisList = List(I(1, "one"), I(2, "two"), I(3, "three"))
    val toReplac = List(I(1, "123"), I(2, "TWO"))
    val resultRP = List(I(1, "123"), I(2, "TWO"), I(3, "three"))

    val result = thisList.replace(toReplac)
    assert(result == resultRP)
  }

  behavior of "list.addOrReplace"

  it should "... not do anything for empty list" in {
    val thisList = List(I(1, "one"), I(2, "two"), I(3, "three"))
    val toReplac = List()

    val result = thisList.addOrReplace(toReplac)
    assert(result == thisList)
  }

  it should "... return nothing when we have empty list" in {
    val thisList = List()
    val toReplac = List(I(1, "one"), I(2, "two"), I(3, "three"))

    val result = thisList.addOrReplace(toReplac)
    assert(result == toReplac)
  }

  it should "... replace the elements that identify the same, or add unidentified ones" in {
    val thisList = List(I(1, "one"), I(2, "two"), I(3, "three"))
    val toReplac = List(I(2, "TWO"), I(3, "$hrEE"), I(4, "I am new!!!"))
    val resultRP = List(I(1, "one"), I(2, "TWO"), I(3, "$hrEE"), I(4, "I am new!!!"))

    val result = thisList.addOrReplace(toReplac)
    assert(result == resultRP)
  }

  behavior of "list.distinctById"

  it should "... return nothing for empty list" in {
    val thisList = List[I]()
    val resultEx = List[I]()

    val result = thisList.distinctById
    assert(result == resultEx)
  }

  it should "... return only elements which identify distinctly" in {
    val thisList = List(I(1, "one"), I(1, "two"), I(3, "three"))
    val resultEx = List(I(1, "one"), I(3, "three"))

    val result = thisList.distinctById
    assert(result == resultEx)
  }

  case class I(
    id:      Int,
    content: String
  )

}
