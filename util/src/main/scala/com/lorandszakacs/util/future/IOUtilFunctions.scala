package com.lorandszakacs.util.future

import scala.collection.generic.CanBuildFrom

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 26 Jun 2017
  *
  */
trait IOUtilFunctions {
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
}
