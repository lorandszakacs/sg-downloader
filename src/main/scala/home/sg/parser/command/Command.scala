package home.sg.parser.command

import scala.util.parsing.combinator.RegexParsers
import com.typesafe.config.impl.Parser
import home.sg.constants.Constants

sealed trait Command

case class Login(val user: String, val password: String) extends Command

case class Download(val sgs: List[String]) extends Command
case class Update(val sgs: List[String]) extends Command

case class Fail(val msg: String) extends Command

object SGCommandParser extends RegexParsers {
  override def skipWhitespace = true;

  def pwd: Parser[String] = "[a-zA-Z][a-zA-Z0-9]*".r
  def login: Parser[Command] = "-login" ~ pwd ^^ {
    case _ ~ password  => Login(Constants.userName, password)
  }

  def sgName: Parser[String] = "[a-zA-Z][a-zA-Z]*".r
  def download: Parser[Command] = "-d" ~ rep(sgName) ^^ {
    case _ ~ List() => Fail(ParserErrorMessages.d_insufficientArguments)
    case _ ~ listOfSgs => Download(listOfSgs)
  }
  
  def update: Parser[Command] = "-u" ~ rep(sgName) ^^ {
    case _ ~ List() => Fail(ParserErrorMessages.u_insufficientArguments)
    case _ ~ listOfSgs => Update(listOfSgs)
  }

  def command = (login | download | update)

  def apply(input: String): Command = parseAll(command, input) match {
    case Success(result, _) => result
    case failure: NoSuccess => Fail(failure.msg)
  }
}


