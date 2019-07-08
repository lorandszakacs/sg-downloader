package com.lorandszakacs.sg.indexer

import com.lorandszakacs.util.effects._
import com.lorandszakacs.sg.http.{SGClient, SGClientAssembly}
import com.lorandszakacs.sg.indexer.impl.SGIndexerImpl

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
trait IndexerAssembly extends SGClientAssembly {
  implicit def httpIOScheduler: HTTPIOScheduler

  def sgIndexer(sgClient: SGClient): SGIndexer = sgIndexerImpl(sgClient)

  //for testing
  private[indexer] def sgIndexerImpl(sgClient: SGClient): SGIndexerImpl =
    new SGIndexerImpl(sgClient)

}
