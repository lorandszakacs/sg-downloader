package com.lorandszakacs.sg.app.repl

import com.lorandszakacs.sg.app.commands.{Command, CommandParser, Commands}
import com.lorandszakacs.sg.downloader.SGDownloaderAssembly
import com.lorandszakacs.util.future._
import com.typesafe.scalalogging.StrictLogging

import scala.util.control.NonFatal

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 01 Jul 2017
  *
  */
class CommandLineInterpreter(
  assembly: SGDownloaderAssembly
) extends StrictLogging {

  private val defaultDays = 160

  def interpretArgs(args: Array[String]): Option[Command] = {
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
    ecx.unsafeRunSync()
  }

  private val downloader = assembly.sgDownloader

  private def eventualInterpretation(args: String): IO[Command] = {
    val triedCommand = CommandParser.parseCommand(args) recoverWith {
      case NonFatal(e) =>
        logger.error(s"failed to parse command: '$args'", e)
        scala.util.Failure(e)
    }

    for {
      command <- IO fromTry triedCommand
      _       <- interpretCommand(command)
    } yield command
  }

  private def interpretCommand(command: Command): IO[Unit] = {
    command match {

      //=======================================================================
      case Commands.DeltaDownload(days, _) =>
        downloader.download.delta(
          daysToExport       = days.getOrElse(defaultDays),
          includeProblematic = true
        )
      //=======================================================================
      case Commands.DownloadSpecific(names, _) =>
        downloader.download.specific(
          names        = names,
          daysToExport = defaultDays
        )
      //=======================================================================
      case Commands.ExportHTML(onlyFavorites) =>
        downloader.export.all(
          daysToExport  = defaultDays,
          onlyFavorites = onlyFavorites
        )

      //=======================================================================
      case Commands.Show(name) =>
        print("\n***************\n")

        downloader.show(name).map(s => println(s))
      //=======================================================================
      case Commands.Favorites =>
        print("\n***************\n")
        downloader.show.favorites.map(s => println(s))
      //=======================================================================
      case Commands.Help =>
        IO.pure {
          val string = Commands.descriptions.map { c =>
            c.fullDescription
          } mkString "\n\n----------------\n\n"
          print(s"----------------\n$string\n")
        }
      //=======================================================================
      case Commands.Exit =>
        IO.pure {
          print("\n-------- exiting --------\n")
        }
      //=======================================================================
    }
  }

}
