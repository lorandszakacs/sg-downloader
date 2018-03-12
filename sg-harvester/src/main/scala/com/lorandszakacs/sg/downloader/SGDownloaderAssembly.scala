package com.lorandszakacs.sg.downloader

import com.lorandszakacs.sg.exporter.SGExporterAssembly
import com.lorandszakacs.sg.indexer.IndexerAssembly
import com.lorandszakacs.sg.model.SGRepoAssembly
import com.lorandszakacs.sg.reifier.ReifierAssembly

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 01 Jul 2017
  *
  */
trait SGDownloaderAssembly {
  this: SGRepoAssembly with IndexerAssembly with ReifierAssembly with SGExporterAssembly =>

  def sgDownloader: SGDownloader = _sgDownloader

  private[downloader] lazy val _sgDownloader = new SGDownloader(
    repo     = sgAndHFRepository,
    indexer  = sgIndexer,
    reifier  = sgReifier,
    exporter = sgExporter
  )

}
