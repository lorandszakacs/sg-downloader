package com.lorandszakacs.util

import monix.eval.Task
import monix.execution.Scheduler

/**
  *
  * @author Lorand Szakacs, https://github.com/lorandszakacs
  * @since 08 Jul 2019
  *
  */
trait TaskLegacySyntax {
  implicit final def taskUnsafeGetSyntaxOps[T](t: Task[T]): TaskLegacySyntax.TaskUnsafeGetOps[T] =
    new TaskLegacySyntax.TaskUnsafeGetOps[T](t)
}

object TaskLegacySyntax {
  final class TaskUnsafeGetOps[T](val t: Task[T]) extends AnyVal {
    import scala.concurrent.duration._
    implicit def unsafeSyncGet(timeout: Duration = Duration.Inf)(implicit sch: Scheduler): T =
      t.runSyncUnsafe(timeout = timeout)
  }
}
