package com.lorandszakacs.sg.downloader

import com.lorandszakacs.sg.Favorites
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
  private[this] val repo: SGAndHFRepository,
  private[this] val indexer: SGIndexer,
  private[this] val reifier: SGReifier,
  private[this] val exporter: SGExporter
)(implicit val executionContext: ExecutionContext) extends StrictLogging {

  /**
    * Used for commands that act on the entire repository
    */
  protected implicit val exporterSettings: ExporterSettings = ExporterSettings(
    favoritesRootFolderPath = "~/sgs/local/models/favorites",
    allMsRootFolderPath = "~/sgs/local/models/all",
    newestRootFolderPath    = "~/sgs/local/models",
    rewriteEverything       = true
  )

  /**
    * Used strictly for the delta export
    */
  protected implicit val deltaExporterSettings: ExporterSettings = ExporterSettings(
    favoritesRootFolderPath = "~/sgs/delta/models/favorites",
    allMsRootFolderPath = "~/sgs/delta/models/all",
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
        _ <- repo.reindexHFs(all)
      } yield ()
    }

    def all(implicit patienceConfig: PatienceConfig = patienceConfig): Future[Unit] = {
      logger.info("... starting index.all, composite of index.allSGs && index.allHFs")
      for {
        _ <- allSGs
        _ <- allHFs
      } yield ()
    }

    def deltaPure(lastProcessedOpt: Option[LastProcessedMarker])(implicit patienceConfig: PatienceConfig): Future[Ms] = {
      logger.info(s"index.delta --> starting from ${lastProcessedOpt.map(_.lastPhotoSetID).getOrElse("")}")
      for {
        newMs: List[M] <- indexer.gatherAllNewMsAndAllTheirPhotoSets(Int.MaxValue, lastProcessedOpt)
        ms = newMs.group
        _ = {
          logger.info(s"finished indexing new entries. Total: #${ms.all.length}")
          logger.info(s"# of new SGs indexed: ${ms.sgs.length}. Names: ${ms.sgNames.stringify}")
          logger.info(s"# of new HFs indexed: ${ms.hfs.length}. Names: ${ms.hfNames.stringify}")
        }
      } yield ms
    }

    def specificPure(modelNames: List[Name])(implicit patienceConfig: PatienceConfig): Future[Ms] = {
      logger.info(s"index.specific --> ${modelNames.stringify}")
      for {
        newMs: List[M] <- Future.serialize(modelNames) { modelName =>
          patienceConfig.throttleAfter {
            indexer.gatherPhotoSetInformationForModel(modelName)
          }
        }
        ms = newMs.group
        _ = {
          logger.info(s"finished indexing specific entries. Total: #${ms.all.length}")
          logger.info(s"# of SGs indexed: ${ms.sgs.length}. Names: ${ms.sgNames.stringify}")
          logger.info(s"# of HFs indexed: ${ms.hfs.length}. Names: ${ms.hfNames.stringify}")
        }
      } yield ms
    }
  }

  object reify {
    def deltaPure(indexedMs: Ms): Future[Ms] = {
      logger.info(s"reify.delta --> reifying indexed Ms # ${indexedMs.all.size}: ${indexedMs.allNames.stringify}")
      for {
        reifiedSGs <- Future.serialize(indexedMs.sgs)(reifier.reifySG)
        reifiedHFs <- Future.serialize(indexedMs.hfs)(reifier.reifyHF)
        reifiedMs = (reifiedSGs, reifiedHFs).group

        _ = {
          logger.info(s"finished reifying new entries. Total: #${reifiedMs.all.length}")
          logger.info(s"# of new SGs reified: ${reifiedSGs.length}")
          logger.info(s"# of new HFs reified: ${reifiedHFs.length}")
        }

      } yield (reifiedSGs, reifiedHFs).group
    }

    def specificPure(indexedMs: Ms): Future[Ms] = {
      logger.info(s"reify.specific --> delagating to reify.delta")
      this.deltaPure(indexedMs)
    }
  }

  object export {

    def delta(daysToExport: Int = 28, delta: List[M]): Future[Unit] = {
      logger.info(s"export.delta --> export. Days to export: $daysToExport. #Ms: ${delta.length}")
      for {
        _ <- exporter.exportDeltaHTMLOfMs(delta)(deltaExporterSettings)
        _ = logger.info(s"export.delta --IMPURE--> finished exporting HTML to ${deltaExporterSettings.newestRootFolderPath}.")
        _ <- exporter.exportLatestForDaysWithDelta(daysToExport, delta)(deltaExporterSettings)
        _ = logger.info(s"export.delta --IMPURE--> finished newest HTML to ${deltaExporterSettings.newestRootFolderPath}.")
      } yield ()
    }

    def specific(daysToExport: Int = 28, delta: List[M]): Future[Unit] = {
      logger.info(s"export.specific --> delagating to export.delta")
      this.delta(daysToExport, delta)
    }
  }

  object write {
    /**
      *
      * @param indexedMs
      * assumes that indexedMs are in the order that they were gathered in initially
      * @return
      */
    def delta(indexedMs: Ms, reifiedMs: Ms, oldLastProcessedMarker: Option[LastProcessedMarker]): Future[Unit] = {
      logger.info(s"write.delta --> writing state to DB. # of fully reified Ms: ${reifiedMs.all.length}")
      logger.info(s"write.delta --> delegating to write.specific")
      for {
        _ <- this.specific(indexedMs, reifiedMs)
        _ = logger.info(s"write.delta --> finished doing write.specific")

        _ <- updateLatestProcessedMarker(indexedMs, reifiedMs, oldLastProcessedMarker)
        _ = logger.info(s"update.delta --IMPURE--> finished writing last processed market to repository")
      } yield ()
    }

    def specific(indexedMs: Ms, reifiedMs: Ms): Future[Unit] = {
      logger.info(s"write.specific --> writing state to DB. # of fully reified Ms: ${reifiedMs.all.length}")
      for {
        _ <- repo.markAsIndexed(indexedMs.hfs, indexedMs.sgs)
        _ = logger.info(s"write.specific --IMPURE--> finished writing SG and HF indexes to repository")

        _ <- repo.createOrUpdateSGs(reifiedMs.sgs)
        _ = logger.info(s"write.specific --IMPURE--> finished writing reified SGs to repository")

        _ <- repo.createOrUpdateHFs(reifiedMs.hfs)
        _ = logger.info(s"write.specific --IMPURE--> finished writing reified HFs to repository")
      } yield ()
    }

    private def updateLatestProcessedMarker(indexedMs: Ms, reifiedMs: Ms, lastProcessedMarker: Option[LastProcessedMarker]): Future[Unit] = {
      logger.info(s"delta.UpdateLatestProcessedIndex: old='${lastProcessedMarker.map(_.lastPhotoSetID).mkString("")}'")
      when(reifiedMs.all.nonEmpty) execute {

        val optNewestM: Option[M] = for {
          newestIndexed <- indexedMs.newestM
          newestReified <- reifiedMs.ml(newestIndexed.name)
        } yield newestReified

        for {
          _ <- when(optNewestM.isEmpty) failWith new IllegalArgumentException("... should have at least one newest gathered")
          newestModel = optNewestM.get
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
        indexedMs <- This.index.deltaPure(lastProcessedOpt)
        _ = logger.info("---------------------------------------------- starting delta.reifying --------------------------------------------")
        reifiedMs <- This.reify.deltaPure(indexedMs)
        _ = logger.info("---------------------------------------------- starting delta.export ----------------------------------------------")
        _ <- This.export.delta(daysToExport, reifiedMs.all)
        _ = logger.info("---------------------------------------------- starting delta.write in DB -----------------------------------------")
        _ <- This.write.delta(indexedMs, reifiedMs, lastProcessedOpt)
        _ = logger.info("---------------------------------------------- finished download.delta -----------------------------------------")
      } yield ()
    }

    def specific(names: List[Name], daysToExport: Int = 28)(implicit passwordProvider: PasswordProvider): Future[Unit] = {
      logger.info("---------------------------------------------- starting download.specific --------------------------------------------")
      logger.info(s"download.specific --> IMPURE --> daysToExport: $daysToExport models: ${names.stringify}")
      for {
        _ <- reifier.authenticateIfNeeded()
        _ = logger.info("---------------------------------------------- starting specific.indexing --------------------------------------------")
        indexedMs <- This.index.specificPure(names)
        _ = logger.info("---------------------------------------------- starting specific.reifying --------------------------------------------")
        reifiedMs <- This.reify.deltaPure(indexedMs)
        _ = logger.info("---------------------------------------------- starting specific.export ----------------------------------------------")
        _ <- This.export.specific(daysToExport, reifiedMs.all)
        _ = logger.info("---------------------------------------------- starting specific.write in DB -----------------------------------------")
        _ <- This.write.specific(indexedMs, reifiedMs)
        _ = logger.info("---------------------------------------------- finished download.specific -----------------------------------------")
      } yield ()
    }
  }

  object show {
    def apply(name: Name): Future[String] = {
      exporter.prettyPrint(name)
    }

    def favorites: Future[String] = {
      Future.successful(Favorites.codeFriendlyDisplay)
    }
  }

  object util {
  }

}



