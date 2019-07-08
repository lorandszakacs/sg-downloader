package com.lorandszakacs.sg.app.repl

import com.lorandszakacs.sg.app.commands.{Command, CommandParser, Commands}
import com.lorandszakacs.sg.downloader.SGDownloaderAssembly
import com.lorandszakacs.util.effects._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 01 Jul 2017
  *
  */
class CommandLineInterpreter(assembly: SGDownloaderAssembly) {
  private val defaultDays = 160

  def interpretArgs(args: Array[String]): Task[Unit] = {
    assert(args.nonEmpty, "why did you call the command line evaluator if you have no command line args?")
    val stringArgs = args.mkString(" ")

    this.interpret(stringArgs).void
  }

  def interpret(args: String): Task[Command] = {
    eventualInterpretation(args)
  }

  private val downloader = assembly.sgDownloader

  private def eventualInterpretation(args: String): Task[Command] = {
    for {
      command <- Task.fromTry(CommandParser.parseCommand(args))
      _       <- interpretCommand(command)
    } yield command
  }

  private def interpretCommand(command: Command): Task[Unit] = {
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
        Task(print("\n***************\n")) >>
          downloader.show(name).map(s => println(s))
      //=======================================================================
      case Commands.Delete(name) =>
        Task(print("\n***************\n")) >>
          downloader.util.delete(name)
      //=======================================================================
      case Commands.Favorites =>
        Task(print("\n***************\n")) >>
          downloader.show.favorites.map(s => println(s))
      //=======================================================================
      case Commands.Help =>
        val string = Commands.descriptions
          .map { c =>
            c.fullDescription
          }
          .mkString("\n\n----------------\n\n")
        Task(print(s"----------------\n$string\n"))
      //=======================================================================
      case Commands.Exit =>
        Task(print("\n-------- exiting --------\n"))
      //=======================================================================
    }
  }

}
