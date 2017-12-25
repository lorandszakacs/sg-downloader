package com.lorandszakacs.util.future

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 26 Jun 2017
  *
  */
trait FutureSyntax {
  this: FutureTypes =>

  /**
    * Used when you want to compose a potentially failing future into a
    * large for comprehension. Helps reduce boilerplate. Instead of writing the usual:
    * {{{
    *   for {
    *     v1 <- Future.successful(1)
    *     v2 <- Future.successful(2)
    *     _ <-   if(v1 < v2) Future.failed(new Exception("I write too much boilerplate :((((")) else Future.successful(())
    *   } yield v1 + v2
    * }}}
    *
    * You can now write:
    *
    * {{{
    * import FutureMonads._
    *
    *  for {
    *    v1 <- Future.successful(1)
    *    v2 <- Future.successful(2)
    *    _ <- when (v1 < v2) failWith new Exception("less boilerplate!")
    *  } yield v1 + v2
    * }}}
    */
  def when(boolExpr: Boolean): FailWithWord = new FailWithWord(boolExpr)

  def when(boolExpr: Future[Boolean]): FailWithWordFuture = new FailWithWordFuture(boolExpr)

  final class FailWithWord private[FutureSyntax] (bool: Boolean) {
    def failWith(exn: => Throwable): Future[Unit] = if (bool) Future.failed(exn) else Future.unit

    def execute(exn: => Future[Unit]): Future[Unit] = if (bool) exn else Future.unit
  }

  final class FailWithWordFuture private[FutureSyntax] (val futureCondition: Future[Boolean]) {

    def failWith(exn: => Throwable)(implicit ec: ExecutionContext): Future[Unit] = {
      futureCondition flatMap { condition =>
        if (condition) Future.failed(exn) else Future.unit
      }
    }

    def execute(exn: => Future[Unit])(implicit ec: ExecutionContext): Future[Unit] = {
      futureCondition flatMap { condition =>
        if (condition) exn else Future.unit
      }
    }
  }
}
