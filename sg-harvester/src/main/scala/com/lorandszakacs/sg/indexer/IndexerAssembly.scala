package com.lorandszakacs.sg.indexer

import akka.actor.ActorSystem
import com.lorandszakacs.sg.http.SGClientAssembly
import com.lorandszakacs.sg.indexer.impl.SGIndexerImpl
import com.lorandszakacs.util.future._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
trait IndexerAssembly extends SGClientAssembly {
  implicit def actorSystem: ActorSystem

  implicit def executionContext: ExecutionContext

  def sgIndexer: SGIndexer = _sgIndexerImpl

  private[indexer] lazy val _sgIndexerImpl = new SGIndexerImpl(suicideGirlsClient)

}