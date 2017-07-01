package com.lorandszakacs.sg.app.repl

import com.lorandszakacs.sg.downloader.SGDownloaderAssembly
import com.lorandszakacs.sg.exporter.ModelExporterAssembly
import com.lorandszakacs.sg.harvester.SGHarvesterAssembly
import com.typesafe.scalalogging.StrictLogging
import com.lorandszakacs.util.future._

import scala.concurrent.duration._
import scala.io.StdIn
import scala.language.postfixOps
import scala.util.control.NonFatal

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 01 Jul 2017
  *
  */
class HarvesterCommandLineEvaluator(
  assembly: SGHarvesterAssembly with
    ModelExporterAssembly with
    SGDownloaderAssembly
) extends StrictLogging {

  private implicit val executionContext: ExecutionContext = assembly.executionContext

  def evaluate(args: Array[String]): Unit = {
    val ecx = eventualEvaluate(args) recover {
      case NonFatal(e) =>
        logger.error(s"Failed to evaluate command: ${args.mkString(" ")}", e)
    }
    ecx.await(24 hours)
  }

  private val downloader = assembly.sgDownloader

  private def eventualEvaluate(args: Array[String]): Future[Unit] = {
    assert(args.nonEmpty, "why did you call the command line evaluator if you have no command line args?")

    def first = args(0)

    for {
      _ <- when(Commands.DeltaUpdate.sameId(first)) execute {
        downloader.delta.update(usernamePasswordConsoleInput)(
          daysToExport = 120,
          includeProblematic = true
        )
      }

      _ <- when(Commands.Help.sameId(first)) execute Future.successful {
        val string = Commands.all.map { c =>
          s"${c.id}: ${c.description}"
        } mkString "\n"
        print(s"$string\n")
      }
    } yield ()
  }

  private lazy val usernamePasswordConsoleInput: () => (String, String) = { () =>
    val username: String = {
      print("\nplease insert username: ")
      StdIn.readLine().trim()
    }
    val plainTextPassword: String = {
      print("\nplease insert password: ")
      StdIn.readLine().trim()
    }
    (username, plainTextPassword)
  }

}
