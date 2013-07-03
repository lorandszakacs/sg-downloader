package home.sg.parser.command

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import home.sg.constants.Constants

@RunWith(classOf[JUnitRunner])
class CommandParserTest extends FunSuite {

  val parser = SGCommandParser
  val defUpdate = Constants.defaultUpdatePath
  val defDownload = Constants.defaultDownloadPath
  val defInputFile = Constants.defaultInputPath

  test("Login command") {
    val result = parser.apply("-login pass45")
    assert(result === Login("Lorand", "pass45"))
  }

  test("Update command, no sg; should be error") {
    val result = parser.apply("-u")
    assert(result === Fail(ParserErrorMessages.u_insufficientArguments))
  }

  test("Update command, invalid sg; should be error") {
    val result = parser.apply("-u 45_Nahp")
    result match {
      case Fail(_) => assert(true);
      case _ => fail("Unexpected success")
    }
  }

  test("Update command, single sg") {
    val result = parser.apply("-u Nahp")
    assert(result === Update(List("Nahp"), defUpdate))
  }

  test("Update command, multiple sg") {
    val result = parser.apply("-u Nahp Sash")
    assert(result === Update(List("Nahp", "Sash"), defUpdate))
  }

  test("UpdateAll command, default file path") {
    val result = parser.apply("-ua")
    assert(result === UpdateAll(defUpdate))
  }

  test("Download command, no sg; should be error") {
    val result = parser.apply("-d")
    assert(result === Fail(ParserErrorMessages.d_insufficientArguments))
  }

  test("Download command, single sg") {
    val result = parser.apply("-d Nahp")
    assert(result === Download(List("Nahp"), defDownload))
  }

  test("Download command, multiple sg") {
    val result = parser.apply("-d Nahp Sash")
    assert(result === Download(List("Nahp", "Sash"), defDownload))
  }

  test("Download From file, default case") {
    val result = parser.apply("-df")
    assert(result === DownloadFromFile(defInputFile, defDownload))
  }

  test("Exit") {
    val result = parser.apply("-exit")
    assert(result === Exit())
  }

}
