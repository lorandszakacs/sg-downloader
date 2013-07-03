package home.sg.parser.command

import scala.util.parsing.combinator.RegexParsers
import com.typesafe.config.impl.Parser
import home.sg.constants.Constants

sealed trait Command

case class Login(val user: String, val password: String) extends Command

case class Update(val sgs: List[String], val folderPath: String) extends Command
case class UpdateAll(val folderPath: String) extends Command

case class Download(val sgs: List[String], val folderPath: String) extends Command
case class DownloadFromFile(val filePath: String, val folderPath: String) extends Command

case class Fail(val msg: String) extends Command

object SGCommandParser extends RegexParsers {
  override def skipWhitespace = true;

  def pwd: Parser[String] = "[a-zA-Z][a-zA-Z0-9]*".r
  def sgName: Parser[String] = "[a-zA-Z][a-zA-Z]*".r
  //TODO: at some point make all commands take an optional command
  //  def path: Parser[String] = "([a-zA-Z]:)?(\\\\[a-zA-Z0-9_.-]+)+\\\\?".r

  def login: Parser[Command] = "-login" ~ pwd ^^ { case _ ~ password => Login(Constants.userName, password) }

  def download: Parser[Command] = "-d" ~ rep(sgName) ^^ {
    case _ ~ List() => Fail(ParserErrorMessages.d_insufficientArguments)
    case _ ~ listOfSgs => Download(listOfSgs, Constants.defaultDownloadPath)
  }

  def downloadFromFile: Parser[Command] = "-df" ^^ { case _ => DownloadFromFile(Constants.defaultInputPath, Constants.defaultDownloadPath) }

  def update: Parser[Command] = "-u" ~ rep(sgName) ^^ {
    case _ ~ List() => Fail(ParserErrorMessages.u_insufficientArguments)
    case _ ~ listOfSgs => Update(listOfSgs, Constants.defaultUpdatePath)
  }

  def updateAll: Parser[Command] = "-ua" ^^ { case _ => UpdateAll(Constants.defaultUpdatePath); }

  def command = (login | downloadFromFile | download | updateAll | update)

  def apply(input: String): Command = parseAll(command, input) match {
    case Success(result, _) => result
    case failure: NoSuccess => Fail(failure.msg)
  }

}


