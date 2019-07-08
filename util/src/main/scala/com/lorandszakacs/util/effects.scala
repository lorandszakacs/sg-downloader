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
    with busymachines.pureharm.effects.PureharmEffectsAllImplicits with ListUtilFunctions with TaskLegacySyntax {

  final type CancellableFuture[T] = monix.execution.CancelableFuture[T]
  final type Scheduler            = monix.execution.Scheduler
  final type Task[T]              = monix.eval.Task[T]

  final val Scheduler: monix.execution.Scheduler.type = monix.execution.Scheduler
  final val Task:      monix.eval.Task.type           = monix.eval.Task

  /**
    *
    * This is an alias for Scheduler that is used to denote that you have blocking IO
    *
    * There is an implicit conversionn from [[DBIOScheduler]] to [[Scheduler]] in the
    * appropriate package
    *
    * @author Lorand Szakacs, lsz@lorandszakacs.com
    * @since 11 Mar 2018
    *
    */
  final case class DBIOScheduler(scheduler: Scheduler)

  /**
    * Analogous to the [[DBIOScheduler]], but for HTTP requests
    *
    */
  final case class HTTPIOScheduler(scheduler: Scheduler)

  object TaskFutureLift {

    def create: FutureLift[Task] = new FutureLift[Task] {
      override def fromFuture[A](fut: => Future[A]): Task[A] = Task.deferFuture(fut)
    }
  }
}
