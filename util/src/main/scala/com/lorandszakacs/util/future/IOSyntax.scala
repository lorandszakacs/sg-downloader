package com.lorandszakacs.util.future

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 26 Jun 2017
  *
  */
trait IOSyntax {
  this: AsyncTypes =>

  /**
    * Used when you want to compose a potentially failing future into a
    * large for comprehension. Helps reduce boilerplate. Instead of writing the usual:
    * {{{
    *   for {
    *     v1 <- IO.pure(1)
    *     v2 <- IO.pure(2)
    *     _ <-   if(v1 < v2) IO.raiseError(new Exception("I write too much boilerplate :((((")) else IO.unit
    *   } yield v1 + v2
    * }}}
    *
    * You can now write:
    *
    * {{{
    * import FutureMonads._
    *
    *  for {
    *    v1 <- IO.pure(1)
    *    v2 <- IO.pure(2)
    *    _ <- when (v1 < v2) failWith new Exception("less boilerplate!")
    *  } yield v1 + v2
    * }}}
    */
  def when(boolExpr: Boolean): FailWithWord = new FailWithWord(boolExpr)

  def when(boolExpr: IO[Boolean]): FailWithWordIO = new FailWithWordIO(boolExpr)

  final class FailWithWord private[IOSyntax] (bool: Boolean) {
    def failWith(exn: => Throwable): IO[Unit] = if (bool) IO.raiseError(exn) else IO.unit

    def execute(exn: => IO[Unit]): IO[Unit] = if (bool) exn else IO.unit
  }

  final class FailWithWordIO private[IOSyntax] (val ioCondition: IO[Boolean]) {

    def failWith(exn: => Throwable): IO[Unit] = {
      ioCondition flatMap { condition =>
        if (condition) IO.raiseError(exn) else IO.unit
      }
    }

    def execute(exn: => IO[Unit]): IO[Unit] = {
      ioCondition flatMap { condition =>
        if (condition) exn else IO.unit
      }
    }
  }
}
