package com.lorandszakacs.sg.app.repl

import com.lorandszakacs.sg.app.commands.{Command, CommandParser, Commands}
import com.lorandszakacs.sg.downloader.SGDownloaderAssembly
import com.lorandszakacs.sg.exporter.ModelExporterAssembly
import com.lorandszakacs.sg.http.PasswordProvider
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
class HarvesterCommandLineEvaluator(
  assembly: ModelExporterAssembly with
    SGDownloaderAssembly
) extends StrictLogging {

  private implicit val executionContext: ExecutionContext = assembly.executionContext

  def evaluate(args: Array[String]): Unit = {
    assert(args.nonEmpty, "why did you call the command line evaluator if you have no command line args?")
    val stringArgs = args.mkString(" ")

    this.evaluate(stringArgs)
  }

  def evaluate(args: String): Unit = {
    val ecx = eventualEvaluate(args) recover {
      case NonFatal(e) =>
        logger.error(s"Failed to evaluate command: $args", e)
    }
    ecx.await(24 hours)
  }

  private val downloader = assembly.sgDownloader

  private def eventualEvaluate(args: String): Future[Unit] = {
    val triedCommand = CommandParser.parseCommand(args) recoverWith {
      case NonFatal(e) =>
        logger.error(s"failed to parse command: '$args'", e)
        scala.util.Failure(e)
    }

    for {
      command <- Future fromTry triedCommand
      _ <- evaluateCommand(command)
    } yield ()
  }

  private def evaluateCommand(command: Command): Future[Unit] = {
    command match {

      //=======================================================================
      case Commands.DeltaHarvest(days, usernameAndPassword) =>
        implicit val ppProvider = optionalPasswordParams(usernameAndPassword)
        //        downloader.delta.update(
        //          daysToExport = days.getOrElse(120),
        //          includeProblematic = true
        //        )
        ???

      //=======================================================================
      case Commands.Help =>
        Future.successful {
          val string = Commands.descriptions.map { c =>
            c.fullDescription
          } mkString "\n----------------\n"
          print(s"----------------\n$string\n")
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
