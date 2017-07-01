package com.lorandszakacs.sg.downloader

import akka.actor.ActorSystem
import com.lorandszakacs.sg.exporter.ModelExporterAssembly
import com.lorandszakacs.sg.harvester.SGHarvesterAssembly
import com.lorandszakacs.sg.indexer.IndexerAssembly
import com.lorandszakacs.sg.model.SGModelAssembly
import com.lorandszakacs.sg.reifier.ReifierAssembly
import com.lorandszakacs.util.future._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 01 Jul 2017
  *
  */
trait SGDownloaderAssembly {
  this: SGModelAssembly with IndexerAssembly with ReifierAssembly with ModelExporterAssembly =>

  implicit def actorSystem: ActorSystem

  implicit def executionContext: ExecutionContext

  def sgDownloader: SGDownloader = _sgDownloader

  private[downloader] lazy val _sgDownloader = new SGDownloader(
    repo = sgModelRepository,
    indexer = sgIndexer,
    reifier = sgReifier,
    exporter = sgExporter
  )(executionContext)

}
