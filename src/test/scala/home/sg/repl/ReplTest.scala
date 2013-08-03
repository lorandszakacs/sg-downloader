package home.sg.repl

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import home.sg.constants.Constants
import home.sg.client.SGClient

@RunWith(classOf[JUnitRunner])
class ReplTest extends FunSuite {
	test("repl"){
	  val client = new SGClient()
	  val repl = new Repl(client)
	  println("please start typing")
	  repl.start;
	}
}