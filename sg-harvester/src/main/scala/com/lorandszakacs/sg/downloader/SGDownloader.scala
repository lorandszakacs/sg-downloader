package com.lorandszakacs.sg.downloader

import com.lorandszakacs.sg.exporter._
import com.lorandszakacs.sg.harvester._
import com.lorandszakacs.sg.http.{PasswordProvider, PatienceConfig}
import com.lorandszakacs.sg.indexer.SGIndexer
import com.lorandszakacs.sg.model._
import com.lorandszakacs.sg.reifier.SGReifier
import com.lorandszakacs.util.future._
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.control.NonFatal

/**
  *
  * Contains an expression of all full end-to-end features that are usable from the command line
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 01 Jul 2017
  *
  */
final class SGDownloader private[downloader](
  protected val repo: SGModelRepository,
  protected val indexer: SGIndexer,
  protected val reifier: SGReifier,
  protected val exporter: SGExporter
)(implicit val executionContext: ExecutionContext) extends StrictLogging {

  /**
    * Used for commands that act on the entire repository
    */
  protected implicit val exporterSettings: ExporterSettings = ExporterSettings(
    favoritesRootFolderPath = "~/sgs/local/models/favorites",
    allModelsRootFolderPath = "~/sgs/local/models/all",
    newestRootFolderPath = "~/sgs/local/models",
    rewriteEverything = true
  )

  /**
    * Used strictly for the delta export
    */
  protected implicit val deltaExporterSettings: ExporterSettings = ExporterSettings(
    favoritesRootFolderPath = "~/sgs/delta/models/favorites",
    allModelsRootFolderPath = "~/sgs/delta/models/all",
    newestRootFolderPath = "~/sgs/delta/models",
    rewriteEverything = true
  )

  protected implicit val patienceConfig: PatienceConfig = PatienceConfig(200 millis)

  object index {
    def allSGs(implicit patienceConfig: PatienceConfig = patienceConfig): Future[Unit] = {
      logger.info("... starting index.allSGs")
      for {
        all <- indexer.gatherSGNames(Int.MaxValue)
        _ = logger.info(s"index.allSGs --> finished gathering ${all.length} SGs")
        _ <- repo.reindexSGs(all)
      } yield ()
    }

    def allHFs(implicit patienceConfig: PatienceConfig = patienceConfig): Future[Unit] = {
      logger.info("... starting index.allHFs")
      for {
        all <- indexer.gatherHFNames(Int.MaxValue)
        _ = logger.info(s"index.allHFs --> finished gathering ${all.length} SGs")
        _ <- repo.reindexHopefuls(all)
      } yield ()
    }

    def all(implicit patienceConfig: PatienceConfig = patienceConfig): Future[Unit] = {
      logger.info("... starting index.all, composite of index.allSGs && index.allHFs")
      for {
        _ <- allSGs
        _ <- allHFs
      } yield ()
    }

//    private[SGDownloader]
//
//    def model(name: ModelName)(implicit patienceConfig: PatienceConfig = patienceConfig): Future[Unit] = {
//      logger.info(s".... starting index.model(${name.name})")
//      for {
//        mode: Model <- indexer.gatherPhotoSetInformationForModel(Model.SuicideGirlFactory)(name) recoverWith {
//          case NonFatal(e) =>
//            indexer.gatherPhotoSetInformationForModel(Model.HopefulFactory)(name)
//        }
//      } yield ()
//    }

  }

  object delta {
    /**
      * This is the most widly used method. It exports a "delta" of what is new since the [[LastProcessedMarker]].
      *
      * It even generates html files accordingly, with new updated indexes, ready to be synced with the already existing
      * export
      *
      * Everything is exported to the paths mentioned in [[deltaExporterSettings]]
      *
      * TODO: settings outght to be read from a config file
      *
      *
      */
    //    def update(daysToExport: Int = 28, includeProblematic: Boolean)(implicit passwordProvider: PasswordProvider): Future[Unit] = {
    //      logger.info(s"starting to do a delta update. Days to export: $daysToExport, includeProblematic: $includeProblematic")
    //      for {
    //        _ <- harvester.authenticateIfNeeded()
    //        allNewHarvested <- harvester.gatherNewestPhotosAndUpdateIndex(Int.MaxValue)
    //        _ = {
    //          val allNewSG = allNewHarvested.keepSuicideGirls
    //          val allNewHopefuls = allNewHarvested.keepHopefuls
    //          logger.info(s"finished harvesting and queuing to reindex all new entries, #${allNewHarvested.length}")
    //          logger.info(s"# of new suicide girls: ${allNewSG.length}. Names: ${allNewSG.map(_.name.name).mkString(",")}")
    //          logger.info(s"# of new hopefuls     : ${allNewHopefuls.length}. Names: ${allNewHopefuls.map(_.name.name).mkString(",")}")
    //        }
    //
    //        allThatNeedUpdating <- harvester.gatherAllDataForSuicideGirlsAndHopefulsThatNeedIndexing(
    //          includeProblematic = includeProblematic
    //        )
    //        _ = {
    //          val (newSGS: List[SuicideGirl], newHopefuls: List[Hopeful]) = allThatNeedUpdating.`SG|Hopeful`
    //          logger.info(s"# of gathered Suicide Girls: ${newSGS.length}")
    //          logger.info(s"# of gathered Hopefuls: ${newHopefuls.length}")
    //        }
    //
    //        _ <- exporter.exportDeltaHTMLIndex(allThatNeedUpdating.map(_.name))(deltaExporterSettings)
    //        _ <- exporter.exportLatestForDays(daysToExport)(deltaExporterSettings)
    //        _ = logger.info("finished writing the delta HTML export.")
    //
    //      } yield ()
    //    }
  }

  object display {
    def model(name: ModelName): Future[String] = {
      exporter.prettyPrint(name)
    }
  }

  object util {
  }

}



