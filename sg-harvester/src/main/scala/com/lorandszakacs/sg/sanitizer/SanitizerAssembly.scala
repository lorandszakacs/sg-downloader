package com.lorandszakacs.sg.sanitizer

import akka.actor.ActorSystem
import com.lorandszakacs.sg.model.SGModelAssembly
import com.lorandszakacs.util.mongodb.Database

import com.lorandszakacs.util.future._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 14 Jul 2017
  *
  */
trait SanitizerAssembly extends SGModelAssembly {
  def db: Database

  implicit def actorSystem: ActorSystem

  implicit def executionContext: ExecutionContext

  def sgSanitizer: SGSanitizer = _sgSanitizerImpl

  private[sanitizer] lazy val _sgSanitizerImpl = new SGSanitizer(sgModelRepository)

}
