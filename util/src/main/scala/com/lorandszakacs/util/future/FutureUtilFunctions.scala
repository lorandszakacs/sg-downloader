package com.lorandszakacs.util.future

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable
import scala.language.postfixOps

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 26 Jun 2017
  *
  */
trait FutureUtilFunctions {
  this: AsyncTypes =>

  implicit class SafeFutureOps[T](f: => Future[T])(implicit ec: ExecutionContext) {
    def suspendInIO: IO[T] = IO.fromFuture(IO(f))
  }

  implicit class IOOps[T](io: IO[T]) {
    def discardValue: IO[Unit] = io.map(_ => ())
  }

  implicit class BuffedIOObject(doNotCare: IO.type) {

    def fromTry[T](tr: scala.util.Try[T]): IO[T] = tr match {
      case scala.util.Failure(e) => IO.raiseError(e)
      case scala.util.Success(v) => IO.pure(v)
    }

    def traverse[A, B, C[X] <: TraversableOnce[X]](col: C[A])(fn: A => IO[B])(
      implicit
      cbf: CanBuildFrom[C[A], B, C[B]]
    ): IO[C[B]] = {
      import scala.collection.mutable

      if (col.isEmpty) {
        IO.pure(cbf.apply().result())
      }
      else {
        val result:  IO[List[B]]              = col.toList.traverse(fn)
        val builder: mutable.Builder[B, C[B]] = cbf.apply()
        result.map(_.foreach(e => builder.+=(e))).map(_ => builder.result())
      }
    }

    def serialize[A, B, C[X] <: TraversableOnce[X]](col: C[A])(fn: A => IO[B])(
      implicit
      cbf: CanBuildFrom[C[A], B, C[B]]
    ): IO[C[B]] = this.traverse(col)(fn)

  }

  implicit class BuffedFutureObject(doNotCare: Future.type) {

    /**
      * Should be used when you wish to apply all the Futures in a serialized way
      *
      * This method ensures that no two futures are executed in parallel.
      *
      * It also guarantees that the serialization of the futures is done
      * in the traversal order of the collection. i.e. If you give it a [[scala.collection.Seq]],
      * then the futures will be executed in the order they appear in the given sequence
      *
      * Implemented as a response to [[Future.sequence]] which does not sequence anything.
      * Syntactically inspired from [[Future.traverse]].
      *
      * Usage:
      *
      * {{{
      *   import com.lorandszakacs.util.future._
      *   val changes: Seq[Change] = //...
      *
      *   //this ensures that no two changes will be applied in parallel.
      *   val applicationOfAll = Future.serialize(changes){change: Change =>
      *     changeService.applyChange(change) recoverWith {
      *       //...
      *     }
      *   }
      *   //... and so on, and so on!
      * }}}
      *
      *
      */
    def serialize[A, B, M[X] <: TraversableOnce[X]](
      traversable: M[A]
    )(fn:          A => Future[B])(implicit cbf: CanBuildFrom[M[A], B, M[B]], executor: ExecutionContext): Future[M[B]] = {
      if (traversable.isEmpty) {
        Future.successful(cbf.apply().result())
      }
      else {
        val seq  = traversable.toSeq
        val head = seq.head
        val tail = seq.tail
        val builder: mutable.Builder[B, M[B]] = cbf.apply()
        val firstBuilder = fn(head) map { z =>
          builder.+=(z)
        }
        val eventualBuilder: Future[mutable.Builder[B, M[B]]] = tail.foldLeft(firstBuilder) {
          (serializedBuilder: Future[mutable.Builder[B, M[B]]], element: A) =>
            serializedBuilder flatMap { (result: mutable.Builder[B, M[B]]) =>
              val f: Future[mutable.Builder[B, M[B]]] = fn(element) map { newElement =>
                result.+=(newElement)
              }
              f
            }
        }
        eventualBuilder map { b =>
          b.result()
        }
      }
    }
  }

  /**
    * Convenience method for quick testing, or for writing trivial stuff
    * like a REPL, should be used with care
    */
  implicit class FutureAwait[T](f: Future[T]) {
    import scala.concurrent.Await
    import scala.concurrent.duration._
    def await(duration: FiniteDuration = 2 minutes): T = Await.result(f, duration)
  }
}
