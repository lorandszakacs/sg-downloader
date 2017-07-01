package com.lorandszakacs.sg.app.repl

import com.lorandszakacs.sg.Favorites
import com.lorandszakacs.sg.downloader.{SGDownloader, SGDownloaderAssembly}
import com.lorandszakacs.sg.exporter.{ExporterSettings, ModelExporterAssembly, SGExporter}
import com.lorandszakacs.sg.harvester.{SGHarvester, SGHarvesterAssembly}
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
class HarvesterRepl(
  assembly: SGHarvesterAssembly with
    ModelExporterAssembly with
    SGDownloaderAssembly
) extends StrictLogging {

  import CommandsDepr._

  private implicit val patienceConfig: PatienceConfig = PatienceConfig(25 millis)
  private implicit val ec: ExecutionContext = assembly.executionContext

  @scala.deprecated("functionality depending on this should be slowly moved to the interpreter", "now")
  private val harvester: SGHarvester = assembly.sgHarvester
  @scala.deprecated("functionality depending on this should be slowly moved to the interpreter", "now")
  private val exporter: SGExporter = assembly.sgExporter

  private val interpreter: SGDownloader = assembly.sgDownloader

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

  private implicit def passwordProviderFromConsole: PasswordProvider = PasswordProvider { () =>
    Future {
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

  private val modelNameConsoleInput: () => ModelName = { () =>
    val model: String = {
      print("\nplease insert modelname: ")
      StdIn.readLine().trim()
    }

    ModelName(model)
  }

  private lazy val consoleEvaluator: HarvesterCommandLineEvaluator =
    new HarvesterCommandLineEvaluator(assembly)

  private class ExitContainer(var should: Boolean = false)

  def start(): Unit = {
    println("type help for instructions")

    val exit = new ExitContainer(should = false)
    while (!exit.should) {
      print("> ")
      val input = StdIn.readLine().trim()

      Try(consoleEvaluator.evaluate(input)) match {
        case Success(_) =>

        case Failure(exception) =>
          //GARBAGE!
          input match {

            //----------------------------------------

            case Exit.id =>
              exit.should = true
            //----------------------------------------

            //----------------------------------------

            case DisplayFavorites.id => interpret {
              print(s"\n${Favorites.codeFriendlyDisplay}\n")
            }

            //----------------------------------------

            case ShowModel.id => interpret {
              print("name (case insensitive): ")
              val name = StdIn.readLine()
              val toDisplay = interpreter.display.model(ModelName(name)).await()
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
              val future = harvester.gatherAllDataForSuicideGirlsAndHopefulsThatNeedIndexing(includeProblematic = true)
              val (newSGS: List[SuicideGirl], newHopefuls: List[Hopeful]) = future.await(12 hours).`SG|Hopeful`
              logger.info(s"# of gathered Suicide Girls: ${newSGS.length}")
              logger.info(s"# of gathered Hopefuls: ${newHopefuls.length}")
            }

            //----------------------------------------

            case GatherAndIndexAll.id => interpret {
              val future = harvester.gatherAllDataForSuicideGirlsAndHopefulsFromScratch()
              val (newSGS: List[SuicideGirl], newHopefuls: List[Hopeful]) = future.await(24 hours).`SG|Hopeful`
              logger.info(s"# of gathered Suicide Girls: ${newSGS.length}")
              logger.info(s"# of gathered Hopefuls: ${newHopefuls.length}")
            }

            //----------------------------------------
            case UpdateSpecific.id =>
              interpret {
                val name = modelNameConsoleInput()
                val f = for {
                  model <- harvester.gatherDataAndUpdateModel(name)
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
      } // end try

    }
  }

}
