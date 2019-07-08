package com.lorandszakacs.sg.app.repl

import com.lorandszakacs.sg.app.commands.{Command, CommandParser, Commands}
import com.lorandszakacs.sg.downloader.SGDownloaderAssembly
import com.lorandszakacs.sg.http.SGClient
import com.lorandszakacs.util.effects._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 01 Jul 2017
  *
  */
class CommandLineInterpreter(
  sgClient: SGClient,
  assembly: SGDownloaderAssembly,
) {
  private val defaultDays = 160

  def interpretArgs(args: List[String]): IO[Unit] = {
    assert(args.nonEmpty, "why did you call the command line evaluator if you have no command line args?")
    val stringArgs = args.mkString(" ")

    this.interpret(stringArgs).void
  }

  def interpret(args: String): IO[Command] = {
    eventualInterpretation(args)
  }

  private val downloader = assembly.sgDownloader(sgClient)

  private def eventualInterpretation(args: String): IO[Command] = {
    for {
      command <- IO.fromTry(CommandParser.parseCommand(args))
      _       <- interpretCommand(command)
    } yield command
  }

  private def interpretCommand(command: Command): IO[Unit] = {
    command match {

      //=======================================================================
      case Commands.DeltaDownload(days, _) =>
        downloader.download.delta(
          daysToExport       = days.getOrElse(defaultDays),
          includeProblematic = true,
        )
      //=======================================================================
      case Commands.DownloadSpecific(names, _) =>
        downloader.download.specific(
          names        = names,
          daysToExport = defaultDays,
        )
      //=======================================================================
      case Commands.ExportHTML(onlyFavorites) =>
        downloader.export.all(
          daysToExport  = defaultDays,
          onlyFavorites = onlyFavorites,
        )
      //=======================================================================
      case Commands.Show(name) =>
        IO(print("\n***************\n")) >>
          downloader.show(name).map(s => println(s))
      //=======================================================================
      case Commands.Delete(name) =>
        IO(print("\n***************\n")) >>
          downloader.util.delete(name)
      //=======================================================================
      case Commands.Favorites =>
        IO(print("\n***************\n")) >>
          downloader.show.favorites.map(s => println(s))
      //=======================================================================
      case Commands.Help =>
        val string = Commands.descriptions
          .map { c =>
            c.fullDescription
          }
          .mkString("\n\n----------------\n\n")
        IO(print(s"----------------\n$string\n"))
      //=======================================================================
      case Commands.Exit =>
        IO(print("\n-------- exiting --------\n"))
      //=======================================================================
    }
  }

}
