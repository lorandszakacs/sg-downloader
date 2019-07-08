package com.lorandszakacs.sg.app.commands

import org.scalatest.flatspec.AnyFlatSpec

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 01 Jul 2017
  *
  */
class AppCommandsParserTest extends AnyFlatSpec {

  //===========================================================================
  //================================ DOWNLOAD =================================
  //===========================================================================
  behavior of "CommandParser download"

  it should "... parse with only one name" in {
    val input  = """download names=nameOne"""
    val result = parse(input)
    assert(result == Commands.DownloadSpecific(names = List("nameOne"), usernameAndPassword = None))
  }

  it should "... parse with multiple names" in {
    val input  = """download names=nameOne,nameTwo,nameThree"""
    val result = parse(input)
    assert(
      result == Commands.DownloadSpecific(names = List("nameOne", "nameTwo", "nameThree"), usernameAndPassword = None),
    )
  }

  it should "... parse with only one name + username, password" in {
    val input  = """download names=nameOne username=someUser password=!@#$sf123AC%^&*()\"/|"""
    val result = parse(input)
    assert(
      result == Commands.DownloadSpecific(
        names               = List("nameOne"),
        usernameAndPassword = Option(("someUser", """!@#$sf123AC%^&*()\"/|""")),
      ),
    )
  }

  it should "... fail when we have trailing comma" in {
    val input = """download names=nameOne,"""
    expectFailure(input)
  }

  it should "... parse with multiple names + username, password" in {
    val input  = """download names=nameOne,nameTwo,nameThree username=someUser password=!@#$sf123AC%^&*()\"/|"""
    val result = parse(input)
    assert(
      result == Commands.DownloadSpecific(
        names               = List("nameOne", "nameTwo", "nameThree"),
        usernameAndPassword = Option(("someUser", """!@#$sf123AC%^&*()\"/|""")),
      ),
    )
  }

  it should "... fail when we have trailing comma and opt username password" in {
    val input = """download names=nameOne,  username=someUser password=!@#$sf123AC%^&*()\"/|"""
    expectFailure(input)
  }

  //===========================================================================
  //================================= DELTA ===================================
  //===========================================================================

  behavior of "CommandParser delta"

  it should "... parse with no arguments" in {
    val input  = "delta"
    val result = parse(input)
    assert(result == Commands.DeltaDownload(days = None, usernameAndPassword = None))
  }

  it should "... parse with only days" in {
    val input  = "delta days=42"
    val result = parse(input)
    assert(result == Commands.DeltaDownload(days = Option(42), usernameAndPassword = None))
  }

  it should "... parse with only username and password" in {
    val input  = """delta username=someUser password=!@#$sf123AC%^&*()\"/|"""
    val result = parse(input)
    assert(
      result == Commands
        .DeltaDownload(days = None, usernameAndPassword = Option(("someUser", """!@#$sf123AC%^&*()\"/|"""))),
    )
  }

  it should "... parse with all parameters -- days first" in {
    val input  = """delta days=42 username=someUser password=!@#$sf123AC%^&*()\"/|"""
    val result = parse(input)
    assert(
      result == Commands
        .DeltaDownload(days = Option(42), usernameAndPassword = Option(("someUser", """!@#$sf123AC%^&*()\"/|"""))),
    )
  }

  //===========================================================================
  //================================= HELP ====================================
  //===========================================================================

  behavior of "CommandParser help"

  it should "... parse simple help" in {
    val input  = "help"
    val result = parse(input)
    assert(result == Commands.Help)
  }

  //===========================================================================
  //===========================================================================
  //===========================================================================

  def parse(input: String): Command =
    CommandParser.parseCommand(input).get

  def expectFailure(input: String): Unit =
    CommandParser.parseCommand(input) match {
      case scala.util.Success(_) =>
        fail(s"... expected $input to fail to parse")

      case scala.util.Failure(_) =>
        ()
    }
}
