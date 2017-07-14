package com.lorandszakacs.sg.app.repl

import com.lorandszakacs.sg.app.commands.Commands
import com.typesafe.scalalogging.StrictLogging

import scala.io.StdIn
import scala.util.{Failure, Success, Try}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
class REPL(
  interpreter: CommandLineInterpreter
) extends StrictLogging {

  private class ExitContainer(var should: Boolean = false)

  def run(): Unit = {
    println("type help for instructions")

    val exit = new ExitContainer(should = false)
    while (!exit.should) {
      print("> ")
      val input = StdIn.readLine().trim()

      Try(interpreter.interpret(input)) match {
        case Success(c) =>
          if (c.contains(Commands.Exit)) {
            exit.should = true
          }

        case Failure(exception) =>
          logger.error(s"failed to interpret: $input", exception)
          print("\n")
      } // end try

    }
  }

}
