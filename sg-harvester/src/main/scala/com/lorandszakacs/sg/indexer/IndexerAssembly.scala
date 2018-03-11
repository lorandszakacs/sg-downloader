package com.lorandszakacs.sg.indexer

import com.lorandszakacs.sg.http.SGClientAssembly
import com.lorandszakacs.sg.indexer.impl.SGIndexerImpl

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
trait IndexerAssembly extends SGClientAssembly {

  def sgIndexer: SGIndexer = _sgIndexerImpl

  private[indexer] lazy val _sgIndexerImpl = new SGIndexerImpl(sgClient)

}
