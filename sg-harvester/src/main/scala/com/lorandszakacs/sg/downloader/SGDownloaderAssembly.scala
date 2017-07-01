package com.lorandszakacs.sg.downloader

import akka.actor.ActorSystem
import com.lorandszakacs.sg.exporter.ModelExporterAssembly
import com.lorandszakacs.sg.harvester.SGHarvesterAssembly
import com.lorandszakacs.util.future._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 01 Jul 2017
  *
  */
trait SGDownloaderAssembly {
  this: SGHarvesterAssembly with ModelExporterAssembly =>

  implicit def actorSystem: ActorSystem

  implicit def executionContext: ExecutionContext

  def sgDownloader: SGDownloader = _sgDownloader

  private[downloader] lazy val _sgDownloader = new SGDownloader(
    harvester = sgHarvester,
    exporter = sgExporter
  )(executionContext)

}
