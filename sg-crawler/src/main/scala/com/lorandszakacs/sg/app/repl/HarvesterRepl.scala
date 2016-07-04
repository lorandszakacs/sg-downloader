package com.lorandszakacs.sg.app.repl

import com.lorandszakacs.sg.harvester.{SGHarvester, SGHarvesterAssembly}
import com.lorandszakacs.sg.http.PatienceConfig

import scala.concurrent.{ExecutionContext, Await}
import scala.concurrent.duration._
import scala.io.StdIn
import scala.language.postfixOps
import scala.util.control.Breaks

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
class HarvesterRepl(harvesterAssembly: SGHarvesterAssembly) {

  case class Command(
    id: String,
    description: String
  )

  private val Help = Command(
    "help",
    "help"
  )

  private val ReindexHopefuls = Command(
    "1",
    """
      |reindex all hopefuls. Harvests from scratch all available hopefuls at:
      |https://www.suicidegirls.com/profiles/hopeful/followers/
    """.stripMargin
  )

  private val ReindexSuicideGirls = Command(
    "2",
    """
      |reindex all suicide girls. Harvests from scratch all available suicide girls at:
      |https://www.suicidegirls.com/profiles/girl/followers/
    """.stripMargin
  )

  private val HarvestNew = Command(
    "3",
    """
      |harvest and reindex new entires. Will mark for reindexing all SGs and hopefulls that were created, from:
      |https://www.suicidegirls.com/photos/all/recent/all/
    """.stripMargin
  )

  private val Exit = Command(
    "0",
    "exit."
  )

  private val all = List(Exit, ReindexHopefuls, ReindexSuicideGirls, HarvestNew)

  private implicit val patienceConfig: PatienceConfig = PatienceConfig(200 millis)
  private implicit val ec: ExecutionContext = harvesterAssembly.executionContext

  private val harvester: SGHarvester = harvesterAssembly.sgHarvester

  def start(): Unit = {
    println("type help for instructions")

    while (true) {
      print("> ")
      val input = StdIn.readLine().trim().toLowerCase
      input match {

        //----------------------------------------

        case Exit.id =>
          Await.ready(harvesterAssembly.shutdown(), 2 minutes)
          System.exit(0)

        //----------------------------------------

        case Help.id =>

          val string = all.map { c =>
            s"${c.id} -> ${c.description}"
          } mkString "\n"
          print {
            s"""
               |
               |$string
               |
            """.stripMargin
          }

        //----------------------------------------

        case ReindexSuicideGirls.id =>
          val future = harvester.reindexSGNames(Int.MaxValue)
          Await.result(future, 2 hours)
          print {
            """
              |
              |-------------- finished harvesting and queing to reindex Suicide Girls --------------
              |
            """.stripMargin
          }


        //----------------------------------------

        case ReindexHopefuls.id =>
          val future = harvester.reindexHopefulsNames(Int.MaxValue)
          Await.result(future, 2 hours)
          print {
            """
              |
              |-------------- finished harvesting and queing to reindex Hopefuls --------------
              |
            """.stripMargin
          }

        //----------------------------------------


        case HarvestNew.id =>
          val future = harvester.gatherNewestPhotosAndUpdateIndex(Int.MaxValue)
          val allNew = Await.result(future, 2 hours)
          print {
            s"""
               |
               |-------------- finished harvesting and queing to reindex all new entries --------------
               |${allNew.map(_.name.name).mkString("\n")}
               |---------------------------------------------------------------------------------------
               |
            """.stripMargin
          }

        //----------------------------------------
      }

    }
  }
}
