package com.lorandszakacs.sg.downloader

import com.lorandszakacs.sg.exporter._
import com.lorandszakacs.sg.http.{PasswordProvider, PatienceConfig}
import com.lorandszakacs.sg.indexer.SGIndexer
import com.lorandszakacs.sg.model._
import com.lorandszakacs.sg.reifier.SGReifier
import com.lorandszakacs.util.future._
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  *
  * Contains an expression of all full end-to-end features that are usable from the command line
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 01 Jul 2017
  *
  */
final class SGDownloader private[downloader](
  private[this] val repo: SGModelRepository,
  private[this] val indexer: SGIndexer,
  private[this] val reifier: SGReifier,
  private[this] val exporter: SGExporter
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
        newModels: List[Model] <- indexer.gatherAllNewModelsAndAllTheirPhotoSets(Int.MaxValue, lastProcessedOpt)
        models = newModels.group
        _ = {
          logger.info(s"finished indexing new entries. Total: #${models.all.length}")
          logger.info(s"# of new suicide girls indexed: ${models.sgs.length}. Names: ${models.sgNames.stringify}")
          logger.info(s"# of new hopefuls indexed     : ${models.hfs.length}. Names: ${models.hfNames.stringify}")
        }
      } yield models
    }

    def specificPure(modelNames: List[ModelName])(implicit patienceConfig: PatienceConfig): Future[Models] = {
      logger.info(s"index.specific --> ${modelNames.stringify}")
      for {
        newModels: List[Model] <- Future.serialize(modelNames) { modelName =>
          patienceConfig.throttleAfter {
            indexer.gatherPhotoSetInformationForModel(modelName)
          }
        }
        models = newModels.group
        _ = {
          logger.info(s"finished indexing specific entries. Total: #${models.all.length}")
          logger.info(s"# of suicide girls indexed: ${models.sgs.length}. Names: ${models.sgNames.stringify}")
          logger.info(s"# of hopefuls indexed     : ${models.hfs.length}. Names: ${models.hfNames.stringify}")
        }
      } yield models
    }
  }

  object reify {
    def deltaPure(indexedModels: Models): Future[Models] = {
      logger.info(s"reify.delta --> reifying indexed models # ${indexedModels.all.size}: ${indexedModels.allNames.stringify}")
      for {
        reifiedSGs <- Future.serialize(indexedModels.sgs)(reifier.reifySuicideGirl)
        reifiedHFs <- Future.serialize(indexedModels.hfs)(reifier.reifyHopeful)
        reifiedModels = (reifiedSGs, reifiedHFs).group

        _ = {
          logger.info(s"finished reifying new entries. Total: #${reifiedModels.all.length}")
          logger.info(s"# of new suicide girls reified: ${reifiedSGs.length}")
          logger.info(s"# of new hopefuls reified     : ${reifiedHFs.length}")
        }

      } yield (reifiedSGs, reifiedHFs).group
    }

    def specificPure(indexedModels: Models): Future[Models] = {
      logger.info(s"reify.specific --> delagating to reify.delta")
      this.deltaPure(indexedModels)
    }
  }

  object export {

    def delta(daysToExport: Int = 28, delta: List[Model]): Future[Unit] = {
      logger.info(s"export.delta --> export. Days to export: $daysToExport. #models: ${delta.length}")
      for {
        _ <- exporter.exportDeltaHTMLOfModels(delta)(deltaExporterSettings)
        _ = logger.info(s"export.delta --IMPURE--> finished exporting HTML to ${deltaExporterSettings.newestRootFolderPath}.")
        _ <- exporter.exportLatestForDaysWithDelta(daysToExport, delta)(deltaExporterSettings)
        _ = logger.info(s"export.delta --IMPURE--> finished newest HTML to ${deltaExporterSettings.newestRootFolderPath}.")
      } yield ()
    }

    def specific(daysToExport: Int = 28, delta: List[Model]): Future[Unit] = {
      logger.info(s"export.specific --> delagating to export.delta")
      this.delta(daysToExport, delta)
    }
  }

  object write {
    /**
      *
      * @param indexedModels
      * assumes that indexedModels are in the order that they were gathered in initially
      * @return
      */
    def delta(indexedModels: Models, reifiedModels: Models, oldLastProcessedMarker: Option[LastProcessedMarker]): Future[Unit] = {
      logger.info(s"write.delta --> writing state to DB. # of fully reified models: ${reifiedModels.all.length}")
      logger.info(s"write.delta --> delegating to write.specific")
      for {
        _ <- this.specific(indexedModels, reifiedModels)
        _ = logger.info(s"write.delta --> finished doing write.specific")

        _ <- updateLatestProcessedMarker(indexedModels, reifiedModels, oldLastProcessedMarker)
        _ = logger.info(s"update.delta --IMPURE--> finished writing last processed market to repository")
      } yield ()
    }

    def specific(indexedModels: Models, reifiedModels: Models): Future[Unit] = {
      logger.info(s"write.specific --> writing state to DB. # of fully reified models: ${reifiedModels.all.length}")
      for {
        _ <- repo.markAsIndexed(indexedModels.hfs, indexedModels.sgs)
        _ = logger.info(s"write.specific --IMPURE--> finished writing SG and HF indexes to repository")

        _ <- repo.createOrUpdateSGs(reifiedModels.sgs)
        _ = logger.info(s"write.specific --IMPURE--> finished writing reified SGs to repository")

        _ <- repo.createOrUpdateHopefuls(reifiedModels.hfs)
        _ = logger.info(s"write.specific --IMPURE--> finished writing reified HFs to repository")
      } yield ()
    }

    private def updateLatestProcessedMarker(indexedModels: Models, reifiedModels: Models, lastProcessedMarker: Option[LastProcessedMarker]): Future[Unit] = {
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
  }

  object download {
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
      */
    def delta(daysToExport: Int = 28, includeProblematic: Boolean)(implicit passwordProvider: PasswordProvider): Future[Unit] = {
      logger.info("---------------------------------------------- starting download.delta --------------------------------------------")
      logger.info(s"download.delta --> IMPURE --> daysToExport: $daysToExport includeProblematic: $includeProblematic")
      for {
        _ <- reifier.authenticateIfNeeded()
        lastProcessedOpt: Option[LastProcessedMarker] <- repo.lastProcessedIndex
        _ = logger.info(s"the last processed set was: ${lastProcessedOpt.map(_.lastPhotoSetID)}")

        _ = logger.info("---------------------------------------------- starting delta.indexing --------------------------------------------")
        indexedModels <- This.index.deltaPure(lastProcessedOpt)
        _ = logger.info("---------------------------------------------- starting delta.reifying --------------------------------------------")
        reifiedModels <- This.reify.deltaPure(indexedModels)
        _ = logger.info("---------------------------------------------- starting delta.export ----------------------------------------------")
        _ <- This.export.delta(daysToExport, reifiedModels.all)
        _ = logger.info("---------------------------------------------- starting delta.write in DB -----------------------------------------")
        _ <- This.write.delta(indexedModels, reifiedModels, lastProcessedOpt)
        _ = logger.info("---------------------------------------------- finished download.delta -----------------------------------------")
      } yield ()
    }

    def specific(modelNames: List[ModelName], daysToExport: Int = 28)(implicit passwordProvider: PasswordProvider): Future[Unit] = {
      logger.info("---------------------------------------------- starting download.specific --------------------------------------------")
      logger.info(s"download.specific --> IMPURE --> daysToExport: $daysToExport models: ${modelNames.stringify}")
      for {
        _ <- reifier.authenticateIfNeeded()
        _ = logger.info("---------------------------------------------- starting specific.indexing --------------------------------------------")
        indexedModels <- This.index.specificPure(modelNames)
        _ = logger.info("---------------------------------------------- starting specific.reifying --------------------------------------------")
        reifiedModels <- This.reify.deltaPure(indexedModels)
        _ = logger.info("---------------------------------------------- starting specific.export ----------------------------------------------")
        _ <- This.export.specific(daysToExport, reifiedModels.all)
        _ = logger.info("---------------------------------------------- starting specific.write in DB -----------------------------------------")
        _ <- This.write.specific(indexedModels, reifiedModels)
        _ = logger.info("---------------------------------------------- finished download.specific -----------------------------------------")
      } yield ()
    }
  }

  object show {
    def apply(name: ModelName): Future[String] = {
      exporter.prettyPrint(name)
    }
  }

  object util {
  }

}



