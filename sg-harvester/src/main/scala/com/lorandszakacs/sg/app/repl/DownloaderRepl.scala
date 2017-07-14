package com.lorandszakacs.sg.app.repl

import com.lorandszakacs.sg.app.commands.Commands
import com.lorandszakacs.sg.exporter.ExporterSettings
import com.lorandszakacs.sg.http.{PasswordProvider, PatienceConfig}
import com.lorandszakacs.sg.model._
import com.lorandszakacs.util.future._
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration._
import scala.io.StdIn
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
class DownloaderRepl(
  interpreter: DownloaderCommandLineInterpreter
)(implicit ec: ExecutionContext) extends StrictLogging {

  private implicit val patienceConfig: PatienceConfig = PatienceConfig(25 millis)

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
          ()
          println()
      } // end try

    }
  }

}
