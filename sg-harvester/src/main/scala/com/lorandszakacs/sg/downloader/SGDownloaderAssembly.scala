package com.lorandszakacs.sg.downloader

import com.lorandszakacs.util.effects._
import com.lorandszakacs.sg.exporter.SGExporterAssembly
import com.lorandszakacs.sg.http.SGClient
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

  implicit def timer: Timer[IO]

  def sgDownloader(sgClient: SGClient): SGDownloader = new SGDownloader(
    repo     = sgAndHFRepository,
    indexer  = sgIndexer(sgClient),
    reifier  = sgReifier(sgClient),
    exporter = sgExporter,
  )

}
