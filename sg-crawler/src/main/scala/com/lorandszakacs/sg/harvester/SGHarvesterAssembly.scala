package com.lorandszakacs.sg.harvester

import akka.actor.ActorSystem
import com.lorandszakacs.sg.crawler.PageCrawlerAssembly
import com.lorandszakacs.sg.harvester.impl.SGHarvesterImpl
import com.lorandszakacs.sg.http.SGClientAssembly
import com.lorandszakacs.sg.model.SGModelAssembly

import scala.concurrent.ExecutionContext

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
trait SGHarvesterAssembly extends PageCrawlerAssembly with SGClientAssembly with SGModelAssembly {

  implicit def actorSystem: ActorSystem

  implicit def executionContext: ExecutionContext

  implicit def sgHarvester: SGHarvester = _sgHarvester

  private[harvester] lazy val _sgHarvester = new SGHarvesterImpl(
    modelAndSetCrawler,
    sgModelRepository
  )
}
