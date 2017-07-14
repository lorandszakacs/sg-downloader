package com.lorandszakacs.sg.app.repl

import com.lorandszakacs.sg.app.commands.{Command, CommandParser, Commands}
import com.lorandszakacs.sg.downloader.SGDownloaderAssembly
import com.lorandszakacs.sg.http.PasswordProvider
import com.lorandszakacs.sg.sanitizer.SanitizerAssembly
import com.lorandszakacs.util.future._
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.control.NonFatal

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 01 Jul 2017
  *
  */
class CommandLineInterpreter(
  assembly: SanitizerAssembly with
    SGDownloaderAssembly
) extends StrictLogging {

  private implicit val executionContext: ExecutionContext = assembly.executionContext

  def interpret(args: Array[String]): Option[Command] = {
    assert(args.nonEmpty, "why did you call the command line evaluator if you have no command line args?")
    val stringArgs = args.mkString(" ")

    this.interpret(stringArgs)
  }

  def interpret(args: String): Option[Command] = {
    val ecx = eventualInterpretation(args).map(Option.apply) recover {
      case NonFatal(e) =>
        logger.error(s"Failed to evaluate command: $args", e)
        None
    }
    ecx.await(24 hours)
  }

  private val downloader = assembly.sgDownloader

  private def eventualInterpretation(args: String): Future[Command] = {
    val triedCommand = CommandParser.parseCommand(args) recoverWith {
      case NonFatal(e) =>
        logger.error(s"failed to parse command: '$args'", e)
        scala.util.Failure(e)
    }

    for {
      command <- Future fromTry triedCommand
      _ <- interpretCommand(command)
    } yield command
  }

  private def interpretCommand(command: Command): Future[Unit] = {
    command match {

      //=======================================================================
      case Commands.DeltaDownload(days, usernameAndPassword) =>
        implicit val ppProvider = optionalPasswordParams(usernameAndPassword)
        downloader.download.delta(
          daysToExport = days.getOrElse(120),
          includeProblematic = true
        )
      //=======================================================================
      case Commands.DownloadSpecific(models, usernameAndPassword) =>
        implicit val ppProvider = optionalPasswordParams(usernameAndPassword)
        downloader.download.specific(
          modelNames = models,
          daysToExport = 120 //TODO: read from commandline
        )
      //=======================================================================
      case Commands.Show(model) =>
        downloader.show(model).map(s => println(s))
      //=======================================================================
      case Commands.Help =>
        Future.successful {
          val string = Commands.descriptions.map { c =>
            c.fullDescription
          } mkString "\n\n----------------\n\n"
          print(s"----------------\n$string\n")
        }
      //=======================================================================
      case Commands.Exit =>
        Future.successful {
          print("\n-------- exiting --------\n")
        }
      //=======================================================================
    }
  }

  private def optionalPasswordParams(opt: Option[(String, String)]): PasswordProvider = PasswordProvider { () =>
    Future fromTry scala.util.Try {
      opt.getOrElse(throw new RuntimeException(".... in the end needed to use password, but none was provided"))
    }
  }

}
