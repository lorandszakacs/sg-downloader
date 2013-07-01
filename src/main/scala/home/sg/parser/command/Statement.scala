package home.sg.parser.command

import scala.util.parsing.combinator.RegexParsers
import com.typesafe.config.impl.Parser
import home.sg.constants.Constants

sealed trait Command

case class Login(val user: String, val password: String) extends Command

case class Update(val sgs: List[String]) extends Command
case class Download(val sgs: List[String]) extends Command

case class Temp(val any: Any) extends Command

object SGCommandParser extends RegexParsers {
  override def skipWhitespace = false;

  def pwd: Parser[String] = "[a-zA-Z][a-zA-Z0-9-]*".r
  def sgName: Parser[String] = "[a-zA-Z]*".r
  

  def login: Parser[Login] = "login" ~ pwd ^^ { case _ ~ password => Login(Constants.userName, password) }
  def update: Parser[Update] = "u:" ~ "{" ~ sgName.* ~ "}" ^^ { case _ ~ _ ~ listOfSgs ~ _ => Update(listOfSgs) }
  
  def testList: Parser[Temp] = rep(sgName ~ " ") ^^ {case l => Temp(l)}

  //  def factor: Parser[Double] = number | "(" ~> expr <~ ")"
  //  def term: Parser[Double] = factor ~ rep("*" ~ factor | "/" ~ factor) ^^ {
  //    case number ~ list => (number /: list) {
  //      case (x, "*" ~ y) => x * y
  //      case (x, "/" ~ y) => x / y
  //    }
  //  }
  //  def expr: Parser[Double] = term ~ rep("+" ~ log(term)("Plus term") | "-" ~ log(term)("Minus term")) ^^ {
  //    case number ~ list => list.foldLeft(number) { // same as before, using alternate name for /:
  //      case (x, "+" ~ y) => x + y
  //      case (x, "-" ~ y) => x - y
  //    }
  //  }
  def command = (login | update)

  def apply(input: String): Command = parseAll(testList, input) match {
    case Success(result, _) => result
    case failure: NoSuccess => {
      println("FAIL")
      scala.sys.error(failure.msg)
    }
  }
}


