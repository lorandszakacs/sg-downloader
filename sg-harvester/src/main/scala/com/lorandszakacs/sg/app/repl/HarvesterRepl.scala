package com.lorandszakacs.sg.app.repl

import com.lorandszakacs.sg.Favorites
import com.lorandszakacs.sg.exporter.{ExporterSettings, ModelDisplayerAssembly, SGExporter}
import com.lorandszakacs.sg.harvester.{SGHarvester, SGHarvesterAssembly}
import com.lorandszakacs.sg.http.PatienceConfig
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
class HarvesterRepl(assembly: SGHarvesterAssembly with ModelDisplayerAssembly) extends StrictLogging {
  case class Command(
    id: String,
    description: String
  )

  private val Help = Command(
    "help",
    "help"
  )

  private val ReindexHopefuls = Command(
    "reindex-hopefuls",
    """
      |reindex all hopefuls. Harvests from scratch all available hopefuls at:
      |https://www.suicidegirls.com/profiles/hopeful/followers/
    """.stripMargin
  )

  private val ReindexSuicideGirls = Command(
    "reindex-suicide-girls",
    """
      |reindex all suicide girls. Harvests from scratch all available suicide girls at:
      |https://www.suicidegirls.com/profiles/girl/followers/
    """.stripMargin
  )

  private val ReindexAll = Command(
    "reindex-all",
    s"""
       |Composite operation of ${ReindexHopefuls.id} and ${ReindexSuicideGirls.id}
    """.stripMargin
  )

  private val GatherNew = Command(
    "gather-new",
    """
      |harvest and reindex new entires. Will mark for reindexing all SGs and hopefuls that were created, from:
      |https://www.suicidegirls.com/photos/all/recent/all/
    """.stripMargin
  )

  private val IndexNew = Command(
    "index-new",
    """
      |gather all set information for the suicidegirls, and hopefuls marked as to index.
      |This requires authentication since it goes on the pages of each model.
    """.stripMargin
  )

  private val GatherAndIndexAll = Command(
    "update-and-reindex-all",
    s"""
       |Gather information about ALL existing suicidegirls and hopefuls, and reparses all their data from the website
    """.stripMargin
  )

  private val UpdateAndIndex = Command(
    "update",
    s"""
       |Fetches latest information from the website, and updates that which needs updating.
       |Composite of ${GatherNew.id} and ${IndexNew.id}
    """.stripMargin
  )

  private val UpdateSpecific = Command(
    "update-specific",
    s"""
       |Fetches latest information from the website, and updates that which needs updating.
       |Composite of gathering information about a specific model, and then index, and then exporting html
    """.stripMargin
  )

  private val CleanIndex = Command(
    "index-clean",
    """
      |clean all models from index that have zero sets on the website
    """.stripMargin
  )

  private val GenerateNewest = Command(
    "newest",
    """
      |generates html containing newest sg-s for the past two months
    """.stripMargin
  )

  private val DetectDuplicateFiles = Command(
    "detect-duplicate-files",
    """
      |if you keep applying delta upgrades to html. You can find a set that was not on FP, suddenly be on FP, in that case the
      |date of the set changes. So you will wind up with two html files. This command will attempt to find such duplicates, and
      |remove them manually. There are some known false alarms. They are filtered out manually
      | """.stripMargin
  )


  private val ShowModel = Command(
    "show",
    """
      |Shows all information about a model.
    """.stripMargin
  )

  private val HtmlFavorites = Command(
    "html-favorites",
    "\nexports a navigable html page of all favorite models @ ~/sgs/local/models/favorites\n"
  )

  private val HtmlAll = Command(
    "html-all",
    "\nexports a navigable html page of all models @ ~/sgs/local/models/all\n"
  )

  private val Exit = Command(
    "exit",
    "\nexit.\n"
  )

  private val DisplayFavorites = Command(
    "favorites",
    "\ndisplay favorites\n"
  )

  private val all = List(Exit, ReindexHopefuls, ReindexSuicideGirls, ReindexAll, GatherNew, IndexNew, CleanIndex, DetectDuplicateFiles, GatherAndIndexAll, UpdateAndIndex, ShowModel, HtmlFavorites, HtmlAll, DisplayFavorites, UpdateSpecific).sortBy(_.id)

  private implicit val patienceConfig: PatienceConfig = PatienceConfig(25 millis)
  private implicit val ec: ExecutionContext = assembly.executionContext

  private val harvester: SGHarvester = assembly.sgHarvester
  private val exporter: SGExporter = assembly.modelDisplayer

  private def interpret(thunk: => Unit): Unit = Try(thunk) match {
    case Success(_) => ()
    case Failure(exception) =>
      logger.error(s"failed because: ${exception.getMessage}", exception)
      ()
      println()
  }

  private implicit val exporterSettings: ExporterSettings = ExporterSettings(
    favoritesRootFolderPath = "~/sgs/local/models/favorites",
    allModelsRootFolderPath = "~/sgs/local/models/all",
    newestRootFolderPath = "~/sgs/local/models",
    rewriteEverything = true
  )

  private implicit val deltaExporterSettings: ExporterSettings = ExporterSettings(
    favoritesRootFolderPath = "~/sgs/delta/models/favorites",
    allModelsRootFolderPath = "~/sgs/delta/models/all",
    newestRootFolderPath = "~/sgs/delta/models",
    rewriteEverything = true
  )

  private val usernamePasswordConsoleInput: () => (String, String) = { () =>
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

  private val modelNameConsoleInput: () => ModelName = { () =>
    val model: String = {
      print("\nplease insert modelname: ")
      StdIn.readLine().trim()
    }

    ModelName(model)
  }

  private def usernamePasswordConstantInput(username: String, plainTextPassword: String): () => (String, String) = { () =>
    (username, plainTextPassword)
  }

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

        case Help.id => interpret {
          val string = all.map { c =>
            s"${c.id}: ${c.description}"
          } mkString "\n"
          print(s"$string\n")
        }

        //----------------------------------------

        case DisplayFavorites.id => interpret {
          print(s"\n${Favorites.codeFriendlyDisplay}\n")
        }

        //----------------------------------------

        case ShowModel.id => interpret {
          print("name (case insensitive): ")
          val name = StdIn.readLine()
          val toDisplay = exporter.prettyPrint(ModelName(name)).await()
          print(s"\n$toDisplay\n")
        }

        //----------------------------------------

        case HtmlFavorites.id => interpret {
          exporter.exportHTMLIndexOfFavorites(exporterSettings).map { _ =>
            logger.info(s"successfully wrote the FAVORITES models index")
          }.await(10 minutes)
        }

        //----------------------------------------

        case HtmlAll.id => interpret {
          exporter.exportHTMLIndexOfAllModels(exporterSettings).map { _ =>
            logger.info(s"successfully wrote ALL the models index")
          }.await(1 hour)

        }

        case ReindexSuicideGirls.id => interpret {
          val allSuicideGirls = harvester.reindexSGNames(Int.MaxValue).await(2 hours)
          logger.info(s"finished reindexing ALL suicide girls. Total number: ${allSuicideGirls.length}")
        }


        //----------------------------------------

        case ReindexHopefuls.id => interpret {
          val allHopefuls = harvester.reindexHopefulsNames(Int.MaxValue).await(2 hours)
          logger.info(s"finished reindexing ALL hopefuls. Total number: ${allHopefuls.length}")
        }
        //----------------------------------------

        case ReindexAll.id => interpret {
          val allModels = harvester.reindexAll(Int.MaxValue).await(4 hours)
          logger.info(s"finished reindexing ALL models. Total number: ${allModels.length}")
        }

        //----------------------------------------

        case GatherNew.id => interpret {
          val allNew = harvester.gatherNewestPhotosAndUpdateIndex(Int.MaxValue).await(2 hours)
          val allNewSG = allNew.keepSuicideGirls
          val allNewHopefuls = allNew.keepHopefuls
          logger.info(s"finished harvesting and queuing to reindex all new entries, #${allNew.length}")
          logger.info(s"# of new suicide girls: ${allNewSG.length}. Names: ${allNewSG.map(_.name.name).mkString(",")}")
          logger.info(s"# of new hopefuls     : ${allNewHopefuls.length}. Names: ${allNewHopefuls.map(_.name.name).mkString(",")}")
        }

        //----------------------------------------

        case IndexNew.id => interpret {
          val input = usernamePasswordConsoleInput
          val future = harvester.gatherAllDataForSuicideGirlsAndHopefulsThatNeedIndexing(input, includeProblematic = true)
          val (newSGS: List[SuicideGirl], newHopefuls: List[Hopeful]) = future.await(12 hours).`SG|Hopeful`
          logger.info(s"# of gathered Suicide Girls: ${newSGS.length}")
          logger.info(s"# of gathered Hopefuls: ${newHopefuls.length}")
        }

        //----------------------------------------

        case GatherAndIndexAll.id => interpret {
          val future = harvester.gatherAllDataForSuicideGirlsAndHopefulsFromScratch(usernamePasswordConsoleInput)
          val (newSGS: List[SuicideGirl], newHopefuls: List[Hopeful]) = future.await(24 hours).`SG|Hopeful`
          logger.info(s"# of gathered Suicide Girls: ${newSGS.length}")
          logger.info(s"# of gathered Hopefuls: ${newHopefuls.length}")
        }

        //----------------------------------------

        case UpdateAndIndex.id =>
          interpret {
            val f = for {
              _ <- harvester.authenticateIfNeeded(usernamePasswordConsoleInput)
              allNewHarvested <- harvester.gatherNewestPhotosAndUpdateIndex(Int.MaxValue)
              _ = {
                val allNewSG = allNewHarvested.keepSuicideGirls
                val allNewHopefuls = allNewHarvested.keepHopefuls
                logger.info(s"finished harvesting and queuing to reindex all new entries, #${allNewHarvested.length}")
                logger.info(s"# of new suicide girls: ${allNewSG.length}. Names: ${allNewSG.map(_.name.name).mkString(",")}")
                logger.info(s"# of new hopefuls     : ${allNewHopefuls.length}. Names: ${allNewHopefuls.map(_.name.name).mkString(",")}")
              }

              allThatNeedUpdating <- harvester.gatherAllDataForSuicideGirlsAndHopefulsThatNeedIndexing(usernamePasswordConsoleInput, includeProblematic = false)
              _ = {
                val (newSGS: List[SuicideGirl], newHopefuls: List[Hopeful]) = allThatNeedUpdating.`SG|Hopeful`
                logger.info(s"# of gathered Suicide Girls: ${newSGS.length}")
                logger.info(s"# of gathered Hopefuls: ${newHopefuls.length}")
              }

              _ <- exporter.exportDeltaHTMLIndex(allThatNeedUpdating.map(_.name))(deltaExporterSettings)
              _ <- exporter.exportLatestForDays(28)(deltaExporterSettings)
              _ = logger.info("finished writing the delta HTML export.")

            } yield ()

            f.await(24 hours)
          }
        //----------------------------------------
        case UpdateSpecific.id =>
          interpret {
            val f = for {
              model <- harvester.gatherDataAndUpdateModel(usernamePasswordConsoleInput, modelNameConsoleInput)
              _ = {
                logger.info(s"finished harvesting ${model.name}")
              }

              _ <- exporter.exportDeltaHTMLIndex(List(model.name))(deltaExporterSettings)
              _ = logger.info("finished writing the delta HTML export.")

            } yield ()
            f.await(24 hours)
          }

        //----------------------------------------

        case GenerateNewest.id => interpret {
          val f = for {
            _ <- exporter.exportLatestForDays(28)(exporterSettings)
          } yield ()
          f.await(1 hour)
        }

        //----------------------------------------

        case CleanIndex.id => interpret {
          val (cleanedSGHs: List[ModelName], cleanedHopefuls: List[ModelName]) = harvester.cleanIndex().await(12 hours)
          logger.info("finished cleaning up")
          logger.info(s"cleaned up suicide girls: ${cleanedSGHs.length}")
          logger.info(s"cleaned up hopefuls: ${cleanedHopefuls.length}")
        }

        //----------------------------------------

        case DetectDuplicateFiles.id => interpret {
          val df = exporter.detectDuplicateFiles("~/Dropbox/Public/suicide-girls/models").await()
          val repr = df.map { duplFilesInFolder =>
            duplFilesInFolder.mkString("\n")
          }
          print {
            s"""|potential duplicates:
                |${repr.mkString("\n\n")}
                |""".stripMargin
          }
        }

        //----------------------------------------

        case unknown =>
          println(s"unknown command: $unknown. Please type help for more information.")
      }

    }
  }

}
