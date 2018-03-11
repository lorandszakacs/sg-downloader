package com.lorandszakacs.sg.downloader

import akka.actor.ActorSystem
import com.lorandszakacs.sg.exporter.SGExporterAssembly
import com.lorandszakacs.sg.indexer.IndexerAssembly
import com.lorandszakacs.sg.model.SGRepoAssembly
import com.lorandszakacs.sg.reifier.ReifierAssembly
import com.lorandszakacs.util.effects._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 01 Jul 2017
  *
  */
trait SGDownloaderAssembly {
  this: SGRepoAssembly with IndexerAssembly with ReifierAssembly with SGExporterAssembly =>

  implicit def actorSystem: ActorSystem

  implicit def executionContext: ExecutionContext

  def sgDownloader: SGDownloader = _sgDownloader

  private[downloader] lazy val _sgDownloader = new SGDownloader(
    repo     = sgAndHFRepository,
    indexer  = sgIndexer,
    reifier  = sgReifier,
    exporter = sgExporter
  )(executionContext)

}
