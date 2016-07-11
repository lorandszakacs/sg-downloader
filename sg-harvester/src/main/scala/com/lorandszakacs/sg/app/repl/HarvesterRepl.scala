package com.lorandszakacs.sg.app.repl

import com.lorandszakacs.sg.harvester.{SGHarvester, SGHarvesterAssembly}
import com.lorandszakacs.sg.http.PatienceConfig
import com.lorandszakacs.sg.model._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.io.StdIn
import scala.language.postfixOps

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

  private val ReindexAll = Command(
    "3",
    s"""
       |Composite operation of ${ReindexHopefuls.id} and ${ReindexSuicideGirls.id}
    """.stripMargin
  )

  private val HarvestNew = Command(
    "4",
    """
      |harvest and reindex new entires. Will mark for reindexing all SGs and hopefuls that were created, from:
      |https://www.suicidegirls.com/photos/all/recent/all/
    """.stripMargin
  )

  private val GatherSetInformation = Command(
    "5",
    """
      |gather all set information for the suicidegirls, and hopefuls marked as to index.
      |This requires authentication since it goes on the pages of each model.
    """.stripMargin
  )
  private val CleanIndex = Command(
    "6",
    """
      |clean all models from index that have zero sets on the website
    """.stripMargin
  )


  private val ShowModel = Command(
    "show",
    """
      |Shows all information about a model.
    """.stripMargin
  )

  private val Exit = Command(
    "exit",
    "\nexit."
  )

  private val all = List(Exit, ReindexHopefuls, ReindexSuicideGirls, ReindexAll, HarvestNew, GatherSetInformation, CleanIndex, ShowModel).sortBy(_.id)

  private implicit val patienceConfig: PatienceConfig = PatienceConfig(200 millis)
  private implicit val ec: ExecutionContext = harvesterAssembly.executionContext

  private val harvester: SGHarvester = harvesterAssembly.sgHarvester
  private val repo: SGModelRepository = harvesterAssembly.sgModelRepository

  def start(): Unit = {
    println("type help for instructions")

    var exit = false
    while (!exit) {
      print("> ")
      val input = StdIn.readLine().trim().toLowerCase
      input match {

        //----------------------------------------

        case Exit.id =>
          exit = true
        //----------------------------------------

        case Help.id =>

          val string = all.map { c =>
            s"${c.id}: ${c.description}"
          } mkString "\n"
          print {
            s"""
               |$string${"\n"}
               |""".stripMargin
          }

        //----------------------------------------

        case ShowModel.id =>
          print("name (case insensitive): ")
          val name = StdIn.readLine()
          val modelName = ModelName(name)
          val future = repo.find(modelName)
          val model = Await.result(future, 1 minute)

          model match {
            case Some(sg: SuicideGirl) =>
              println {
                sg.reverseSets.toString
              }

            case Some(hopeful: Hopeful) =>
              println {
                hopeful.reverseSets.toString
              }

            case None =>
              println(s"could not find model ${modelName.name}")
          }

        //----------------------------------------

        case ReindexSuicideGirls.id =>
          val future = harvester.reindexSGNames(Int.MaxValue)
          Await.result(future, 2 hours)
          print {
            s"""|
                |-------------- finished harvesting and queuing to reindex Suicide Girls --------------
                |""".stripMargin
          }


        //----------------------------------------

        case ReindexHopefuls.id =>
          val future = harvester.reindexHopefulsNames(Int.MaxValue)
          Await.result(future, 2 hours)
          print {
            s"""|
                |-------------- finished harvesting and queuing to reindex Hopefuls --------------
                |""".stripMargin
          }

        //----------------------------------------

        case ReindexAll.id =>
          val future = harvester.reindexAll(Int.MaxValue)
          Await.result(future, 4 hours)
          print {
            s"""|
                |-------------- finished harvesting and queuing to reindex all --------------
                |""".stripMargin
          }

        //----------------------------------------


        case HarvestNew.id =>
          val future = harvester.gatherNewestPhotosAndUpdateIndex(Int.MaxValue)
          val allNew = Await.result(future, 2 hours)
          print {
            s"""
               |
               |-------------- finished harvesting and queuing to reindex all new entries -------------
               |${allNew.map(_.name.name).mkString("\n")}
               |---------------------------------------------------------------------------------------
               |""".stripMargin
          }

        //----------------------------------------

        case GatherSetInformation.id =>
          val username: String = {
            print("\nplease insert username: ")
            StdIn.readLine().trim()
          }
          val plainTextPassword: String = {
            print("\nplease insert password: ")
            StdIn.readLine().trim()
          }
          val future = harvester.gatherAllDataForSuicideGirlsAndHopefulsThatNeedIndexing(username, plainTextPassword)
          val (newSGS: List[SuicideGirl], newHopefuls: List[Hopeful]) = Await.result(future, 12 hours).`SG|Hopeful`
          print {
            s"""
               |Suicide Girls: ${newSGS.length}
               |Hopefuls: ${newHopefuls.length}
               |""".stripMargin
          }

        //----------------------------------------
        case CleanIndex.id =>

          val future = harvester.cleanIndex()
          val (cleanedSGHs: List[ModelName], cleanedHopefuls: List[ModelName]) = Await.result(future, 12 hour)
          print {
            s"""
               |Cleaned up:
               |Suicide Girls: ${cleanedSGHs.length}
               |Hopefuls: ${cleanedHopefuls.length}
               |""".stripMargin
          }


        case unknown =>
          println(s"unknown command: $unknown. Please type help for more information.")
      }

    }
  }
}
