package com.lorandszakacs.sg.downloader

import com.lorandszakacs.sg.exporter._
import com.lorandszakacs.sg.harvester._
import com.lorandszakacs.sg.http.{PasswordProvider, PatienceConfig}
import com.lorandszakacs.sg.indexer.SGIndexer
import com.lorandszakacs.sg.model._
import com.lorandszakacs.sg.reifier.SGReifier
import com.lorandszakacs.util.future._
import com.typesafe.scalalogging.StrictLogging
import shapeless._

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

    def deltaPure(lastProcessedOpt: Option[LastProcessedMarker])(implicit patienceConfig: PatienceConfig): Future[Models] = {
      logger.info(s"index.delta --> starting from ${lastProcessedOpt.map(_.lastPhotoSetID).getOrElse("")}")
      for {
        newModels: List[Model] <- indexer.gatherNewestModelInformation(Int.MaxValue, lastProcessedOpt)
        models = newModels.group
        _ = {
          logger.info(s"finished indexing new entries. Total: #${models.all.length}")
          logger.info(s"# of new suicide girls indexed: ${models.sgs.length}. Names: ${models.sgs.map(_.name.name).mkString(",")}")
          logger.info(s"# of new hopefuls indexed     : ${models.hfs.length}. Names: ${models.hfs.map(_.name.name).mkString(",")}")
        }
      } yield models
    }
  }

  object reify {
    def deltaPure(indexedModels: Models)(implicit passwordProvider: PasswordProvider): Future[Models] = {
      for {
        reifiedSGs <- Future.traverse(indexedModels.sgs)(reifier.reifySuicideGirl)
        reifiedHFs <- Future.traverse(indexedModels.hfs)(reifier.reifyHopeful)
        reifiedModels = (reifiedSGs, reifiedHFs).group

        _ = {
          logger.info(s"finished reifying new entries. Total: #${reifiedModels.all.length}")
          logger.info(s"# of new suicide girls reified: ${reifiedSGs.length}")
          logger.info(s"# of new hopefuls reified     : ${reifiedHFs.length}")
        }

      } yield (reifiedSGs, reifiedHFs).group
    }
  }

  object export {
    private val This = SGDownloader.this

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
    def delta(daysToExport: Int = 28, includeProblematic: Boolean)(implicit passwordProvider: PasswordProvider): Future[Unit] = {
      logger.info(s"export.delta --> starting to do a delta update. Days to export: $daysToExport, includeProblematic: $includeProblematic")
      for {
        _ <- reifier.authenticateIfNeeded()
        lastProcessedOpt: Option[LastProcessedMarker] <- repo.lastProcessedIndex
        _ = logger.info(s"the last processed set was: ${lastProcessedOpt.map(_.lastPhotoSetID)}")

        indexedModels <- This.index.deltaPure(lastProcessedOpt)
        reifiedModels <- This.reify.deltaPure(indexedModels)

        _ <- exporter.exportDeltaHTMLIndex(reifiedModels.all.map(_.name))(deltaExporterSettings)
        _ = logger.info(s"export.delta --IMPURE--> finished exporting HTML to ${deltaExporterSettings.newestRootFolderPath}.")
        _ <- exporter.exportLatestForDays(daysToExport)(deltaExporterSettings)
        _ = logger.info(s"export.delta --IMPURE--> finished newest HTML to ${deltaExporterSettings.newestRootFolderPath}.")

        _ <- repo.updateIndexes(indexedModels.hfs, indexedModels.sgs)
        _ = logger.info(s"export.delta --IMPURE--> finished writing SG and HF indexes to repository")

        _ <- repo.createOrUpdateSGs(reifiedModels.sgs)
        _ = logger.info(s"export.delta --IMPURE--> finished writing reified SGs to repository")

        _ <- repo.createOrUpdateHopefuls(reifiedModels.hfs)
        _ = logger.info(s"export.delta --IMPURE--> finished writing reified HFs to repository")

        _ <- updateLastestProcessedMarkerImpure(indexedModels, reifiedModels, lastProcessedOpt)
        _ = logger.info(s"export.delta --IMPURE--> finished writing last processed market to repository")
      } yield ()
    }

    private def updateLastestProcessedMarkerImpure(indexedModels: Models, reifiedModels: Models, lastProcessedMarker: Option[LastProcessedMarker]): Future[Unit] = {
      logger.info(s"delta.UpdateLatestProcessedIndex: old='${lastProcessedMarker.map(_.lastPhotoSetID).mkString("")}'")
      when(reifiedModels.all.nonEmpty) execute {

        val optNewestModel: Option[Model] = for {
          newestIndexed <- indexedModels.newestModel
          newestReified <- reifiedModels.ml(newestIndexed.name)
        } yield newestReified

        for {
          _ <- when(optNewestModel.isEmpty) failWith new IllegalArgumentException("... should have at least one newest gathered")
          newestModel = optNewestModel.get
          newMarker = indexer.createLastProcessedIndex(newestModel)
          _ = logger.info(s"delta.UpdateLatestProcessedIndex: new='${newMarker.lastPhotoSetID}'")
          _ <- repo.createOrUpdateLastProcessed(newMarker)
        } yield ()

        Future.unit
      }
    }

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



