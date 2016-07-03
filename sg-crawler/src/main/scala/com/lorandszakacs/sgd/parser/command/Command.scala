/**
 * Copyright 2015 Lorand Szakacs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.lorandszakacs.sgd.parser.command

import scala.util.parsing.combinator.RegexParsers

/**
 * @author Lorand Szakacs, lsz@lorandszakacs.com
 * @since 16 Mar 2015
 *
 */
sealed trait Command {
  def instructions: String

  def command: String

  def man: String = this.command + " " + this.instructions
}

case class Login(user: String, password: String) extends Command {
  override def command: String = "-login"

  override def instructions: String =
    "password; username is extracted from the application.conf file. Current user = "
}

case class Update(sgs: List[String], folderPath: String) extends Command {
  override def instructions: String =
    "sg1 sg2 ...; the update path is extracted from application.conf. Current default path is= "

  override def command: String = "-u"
}

case class UpdateAll(folderPath: String) extends Command {
  override def instructions: String =
    "; the program will look at the default folder specified in the config file. Current default path is= "

  override def command: String = "-ua "
}

case class Download(sgs: List[String], folderPath: String) extends Command {
  override def instructions: String =
    "sg1 sg2 ...; the folder where to download is read from the config file. Current default path is= "

  override def command: String = "-d"
}

case class DownloadFromFile(filePath: String, folderPath: String) extends Command {
  override def instructions: String =
    "; the sg names are read from a file specified in the config file. Current input file is= \n default download folder path = "

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

case class Fail(msg: String) extends Command {
  override def instructions: String = ""

  override def command: String = ""
}

object SGCommandParser extends RegexParsers {
  override def skipWhitespace = true

  def pwd: Parser[String] = "[a-zA-Z][a-zA-Z0-9]*".r

  def sgName: Parser[String] = "[a-zA-Z][a-zA-Z0-9_]*".r

  //TODO: at some point make all commands take an optional command
  //  def path: Parser[String] = "([a-zA-Z]:)?(\\\\[a-zA-Z0-9_.-]+)+\\\\?".r

  def login: Parser[Command] = "-login" ~ pwd ^^ { case _ ~ password => Login("", password)}

  def download: Parser[Command] = "-d" ~ rep(sgName) ^^ {
    case _ ~ List() => Fail(ParserErrorMessages.DownloadInsufficientArguments)
    case _ ~ listOfSgs => Download(listOfSgs, "")
  }

  def downloadFromFile: Parser[Command] = "-df" ^^ { _ => DownloadFromFile("", "")}

  def update: Parser[Command] = "-u" ~ rep(sgName) ^^ {
    case _ ~ List() => Fail(ParserErrorMessages.UpdateInsufficientArguments)
    case _ ~ listOfSgs => Update(listOfSgs, "")
  }

  def help: Parser[Command] = "-help" ^^ { _ => Help()}

  def updateAll: Parser[Command] = "-ua" ^^ { _ => UpdateAll("")}

  def exit: Parser[Command] = "-exit" ^^ { _ => Exit()}

  def command = login | downloadFromFile | download | updateAll | update | exit | help

  def apply(input: String): Command = parseAll(command, input) match {
    case Success(result, _) => result
    case failure: NoSuccess => Fail(failure.msg)
  }

}

