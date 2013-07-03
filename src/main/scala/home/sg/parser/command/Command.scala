package home.sg.parser.command

import scala.util.parsing.combinator.RegexParsers
import com.typesafe.config.impl.Parser
import home.sg.constants.Constants

sealed trait Command {
  def instructions(): String
}

case class Login(val user: String, val password: String) extends Command {
  override def instructions(): String = {
    "-login password; username is extracted from the application.conf file"
  }
}

case class Update(val sgs: List[String], val folderPath: String) extends Command {
  override def instructions(): String = {
    "-u sg1 sg2 ...; the update path is extracted from application.conf"
  }
}
case class UpdateAll(val folderPath: String) extends Command {
  override def instructions(): String = {
    "-ua ; the program will look at the default folder specified in the config file"
  }
}

case class Download(val sgs: List[String], val folderPath: String) extends Command {
  override def instructions(): String = {
    "-d sg1 sg2 ...; the folder where to download is read from the config file"
  }
}
case class DownloadFromFile(val filePath: String, val folderPath: String) extends Command {
  override def instructions(): String = {
    "-df ; the sg names are read from a file specified in the config file."
  }
}

case class Exit() extends Command {
  override def instructions(): String = {
    ""
  }
}

case class Fail(val msg: String) extends Command {
  override def instructions(): String = {
    ""
  }
}

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

  def exit: Parser[Command] = "-exit" ^^ { case _ => Exit() }

  def command = (login | downloadFromFile | download | updateAll | update | exit)

  def apply(input: String): Command = parseAll(command, input) match {
    case Success(result, _) => result
    case failure: NoSuccess => Fail(failure.msg)
  }

}


