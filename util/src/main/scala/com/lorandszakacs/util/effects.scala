package com.lorandszakacs.util

import com.lorandszakacs.util.list.ListUtilFunctions

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 11 Mar 2018
  *
  */
object effects
    extends AnyRef with busymachines.pureharm.effects.PureharmEffectsAllTypes
    with busymachines.pureharm.effects.PureharmEffectsAllImplicits with ListUtilFunctions with IOLegacySyntax {

  /**
    *
    * This is an alias for ExecutionContext that is used to denote that you have blocking IO
    *
    * There is an implicit conversionn from [[DBIOScheduler]] to [[ExecutionContext]] in the
    * appropriate package
    *
    * @author Lorand Szakacs, lsz@lorandszakacs.com
    * @since 11 Mar 2018
    *
    */
  final case class DBIOScheduler(scheduler: ExecutionContext)

  /**
    * Analogous to the [[DBIOScheduler]], but for HTTP requests
    *
    */
  final case class HTTPIOScheduler(scheduler: ExecutionContext)

}
