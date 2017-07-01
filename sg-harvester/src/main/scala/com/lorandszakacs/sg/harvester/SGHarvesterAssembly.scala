package com.lorandszakacs.sg.harvester

import akka.actor.ActorSystem
import com.lorandszakacs.sg.harvester.impl.SGHarvesterImpl
import com.lorandszakacs.sg.http.SGClientAssembly
import com.lorandszakacs.sg.indexer.IndexerAssembly
import com.lorandszakacs.sg.model.SGModelAssembly
import com.lorandszakacs.sg.reifier.ReifierAssembly
import com.lorandszakacs.util.future._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
@scala.deprecated("replaced by non-redundant downloader", "now")
trait SGHarvesterAssembly extends ReifierAssembly with IndexerAssembly with SGClientAssembly {
  this: SGModelAssembly =>

  implicit def actorSystem: ActorSystem

  implicit def executionContext: ExecutionContext

  def sgHarvester: SGHarvester = _sgHarvester

  private[harvester] lazy val _sgHarvester = new SGHarvesterImpl(
    sgIndexer,
    sgReifier,
    sgModelRepository
  )(executionContext)

}
