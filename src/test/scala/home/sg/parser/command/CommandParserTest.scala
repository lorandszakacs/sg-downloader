package home.sg.parser.command

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CommandParserTest extends FunSuite {

  val parser = SGCommandParser

  test("Login command") {
    val result = parser.apply("-login pass45")
    assert(result === Login("Lorand", "pass45"))
  }

  test("Update command, no sg; should be error") {
    val result = parser.apply("-u")
    assert(result == Fail(ParserErrorMessages.u_insufficientArguments))
  }

  test("Update command, single sg") {
    val result = parser.apply("-u Nahp")
    assert(result == Update(List("Nahp")))
  }

  test("Update command, multiple sg") {
    val result = parser.apply("-u Nahp Sash")
    assert(result == Update(List("Nahp", "Sash")))
  }
}
