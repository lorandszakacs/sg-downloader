package home.sg.parser.command

import scala.util.parsing.combinator.RegexParsers
import com.typesafe.config.impl.Parser
import home.sg.constants.Constants

sealed trait Command {
  def instructions: String
  def command: String
  def man: String = this.command + " " + this.instructions
}

case class Login(val user: String, val password: String) extends Command {
  override def command: String = "-login"
  override def instructions: String =
    "password; username is extracted from the application.conf file. Current user = " + Constants.UserName
}

case class Update(val sgs: List[String], val folderPath: String) extends Command {
  override def instructions: String =
    "sg1 sg2 ...; the update path is extracted from application.conf. Current default path is= " + Constants.DefaultUpdatePath
  override def command: String = "-u"
}
case class UpdateAll(val folderPath: String) extends Command {
  override def instructions: String =
    "; the program will look at the default folder specified in the config file. Current default path is= " + Constants.DefaultUpdatePath
  override def command: String = "-ua "
}

case class Download(val sgs: List[String], val folderPath: String) extends Command {
  override def instructions: String =
    "sg1 sg2 ...; the folder where to download is read from the config file. Current default path is= " + Constants.DefaultDownloadPath
  override def command: String = "-d"
}
case class DownloadFromFile(val filePath: String, val folderPath: String) extends Command {
  override def instructions: String =
    "; the sg names are read from a file specified in the config file. Current input file is= " + Constants.DefaultInputPath + "\n default download folder path = " + Constants.DefaultDownloadPath
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
  def sgName: Parser[String] = "[a-zA-Z][a-zA-Z]*".r
  //TODO: at some point make all commands take an optional command
  //  def path: Parser[String] = "([a-zA-Z]:)?(\\\\[a-zA-Z0-9_.-]+)+\\\\?".r

  def login: Parser[Command] = "-login" ~ pwd ^^ { case _ ~ password => Login(Constants.UserName, password) }

  def download: Parser[Command] = "-d" ~ rep(sgName) ^^ {
    case _ ~ List() => Fail(ParserErrorMessages.DownloadInsufficientArguments)
    case _ ~ listOfSgs => Download(listOfSgs, Constants.DefaultDownloadPath)
  }

  def downloadFromFile: Parser[Command] = "-df" ^^ { _ => DownloadFromFile(Constants.DefaultInputPath, Constants.DefaultDownloadPath) }

  def update: Parser[Command] = "-u" ~ rep(sgName) ^^ {
    case _ ~ List() => Fail(ParserErrorMessages.UpdateInsufficientArguments)
    case _ ~ listOfSgs => Update(listOfSgs, Constants.DefaultUpdatePath)
  }

  def help: Parser[Command] = "-help" ^^ { _ => Help() }

  def updateAll: Parser[Command] = "-ua" ^^ { _ => UpdateAll(Constants.DefaultUpdatePath); }

  def exit: Parser[Command] = "-exit" ^^ { _ => Exit() }

  def command = (login | downloadFromFile | download | updateAll | update | exit | help)

  def apply(input: String): Command = parseAll(command, input) match {
    case Success(result, _) => result
    case failure: NoSuccess => Fail(failure.msg)
  }

}

