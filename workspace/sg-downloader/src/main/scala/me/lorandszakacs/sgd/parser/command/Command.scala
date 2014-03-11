/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Lorand Szakacs
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
package me.lorandszakacs.sgd.parser.command

import scala.util.parsing.combinator.RegexParsers

import me.lorandszakacs.sgd.constants.ConfigValues

sealed trait Command {
  def instructions: String
  def command: String
  def man: String = this.command + " " + this.instructions
}

case class Login(val user: String, val password: String) extends Command {
  override def command: String = "-login"
  override def instructions: String =
    "password; username is extracted from the application.conf file. Current user = " + ConfigValues.UserName
}

case class Update(val sgs: List[String], val folderPath: String) extends Command {
  override def instructions: String =
    "sg1 sg2 ...; the update path is extracted from application.conf. Current default path is= " + ConfigValues.DefaultUpdatePath
  override def command: String = "-u"
}
case class UpdateAll(val folderPath: String) extends Command {
  override def instructions: String =
    "; the program will look at the default folder specified in the config file. Current default path is= " + ConfigValues.DefaultUpdatePath
  override def command: String = "-ua "
}

case class Download(val sgs: List[String], val folderPath: String) extends Command {
  override def instructions: String =
    "sg1 sg2 ...; the folder where to download is read from the config file. Current default path is= " + ConfigValues.DefaultDownloadPath
  override def command: String = "-d"
}
case class DownloadFromFile(val filePath: String, val folderPath: String) extends Command {
  override def instructions: String =
    "; the sg names are read from a file specified in the config file. Current input file is= " + ConfigValues.DefaultInputPath + "\n default download folder path = " + ConfigValues.DefaultDownloadPath
  override def command: String = "-df"
}

case class Help() extends Command {
  override def instructions: String =
    ""
  override def command: String = "-help"
}

case class Exit() extends Command {
  override def instructions: String = "exits the program. No login info is ever stored"
  override def command: String = "-exit"
}

case class Fail(val msg: String) extends Command {
  override def instructions: String = ""
  override def command: String = ""
}

object SGCommandParser extends RegexParsers {
  override def skipWhitespace = true;

  def pwd: Parser[String] = "[a-zA-Z][a-zA-Z0-9]*".r
  def sgName: Parser[String] = "[a-zA-Z][a-zA-Z0-9_]*".r
  //TODO: at some point make all commands take an optional command
  //  def path: Parser[String] = "([a-zA-Z]:)?(\\\\[a-zA-Z0-9_.-]+)+\\\\?".r

  def login: Parser[Command] = "-login" ~ pwd ^^ { case _ ~ password => Login(ConfigValues.UserName, password) }

  def download: Parser[Command] = "-d" ~ rep(sgName) ^^ {
    case _ ~ List() => Fail(ParserErrorMessages.DownloadInsufficientArguments)
    case _ ~ listOfSgs => Download(listOfSgs, ConfigValues.DefaultDownloadPath)
  }

  def downloadFromFile: Parser[Command] = "-df" ^^ { _ => DownloadFromFile(ConfigValues.DefaultInputPath, ConfigValues.DefaultDownloadPath) }

  def update: Parser[Command] = "-u" ~ rep(sgName) ^^ {
    case _ ~ List() => Fail(ParserErrorMessages.UpdateInsufficientArguments)
    case _ ~ listOfSgs => Update(listOfSgs, ConfigValues.DefaultUpdatePath)
  }

  def help: Parser[Command] = "-help" ^^ { _ => Help() }

  def updateAll: Parser[Command] = "-ua" ^^ { _ => UpdateAll(ConfigValues.DefaultUpdatePath); }

  def exit: Parser[Command] = "-exit" ^^ { _ => Exit() }

  def command = (login | downloadFromFile | download | updateAll | update | exit | help)

  def apply(input: String): Command = parseAll(command, input) match {
    case Success(result, _) => result
    case failure: NoSuccess => Fail(failure.msg)
  }

}

