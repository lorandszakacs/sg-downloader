/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013 Lorand Szakacs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package home.sg.parser.command

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import home.sg.constants.Constants

@RunWith(classOf[JUnitRunner])
class CommandParserTest extends FunSuite {

  val parser = SGCommandParser
  val defUpdate = Constants.DefaultUpdatePath
  val defDownload = Constants.DefaultDownloadPath
  val defInputFile = Constants.DefaultInputPath

  test("Login command") {
    val result = parser.apply("-login pass45")
    assert(result === Login("Lorand", "pass45"))
  }

  test("Update command, no sg; should be error") {
    val result = parser.apply("-u")
    assert(result === Fail(ParserErrorMessages.UpdateInsufficientArguments))
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
    assert(result === Fail(ParserErrorMessages.DownloadInsufficientArguments))
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
