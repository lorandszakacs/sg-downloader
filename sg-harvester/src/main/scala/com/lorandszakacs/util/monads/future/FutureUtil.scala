package com.lorandszakacs.util.monads.future

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable
import scala.language.higherKinds

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 05 Jul 2016
  *
  */
object FutureUtil {
  /**
    * convenience aliases, so that you don't have to import [[FutureUtil]] and [[scala.concurrent.Future]]
    * at the same time
    */
  val Future: concurrent.Future.type = concurrent.Future
  type Future[+T] = concurrent.Future[T]
  type ExecutionContext = concurrent.ExecutionContext

  val UnitFunction: Any => Unit = { _ => () }

  val FutureUnitFunction: Any => Future[Unit] = { _ => Future.unit }

  implicit class BuffedFutureObject(dontCare: Future.type) {

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
      *   import FutureMonads._
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
    def serialize[A, B, M[X] <: TraversableOnce[X]](traversable: M[A])(fn: A => concurrent.Future[B])(implicit cbf: CanBuildFrom[M[A], B, M[B]], executor: ExecutionContext): Future[M[B]] = {
      if (traversable.isEmpty) {
        Future.successful(cbf.apply().result())
      } else {
        val seq = traversable.toSeq
        val head = seq.head
        val tail = seq.tail
        val builder: mutable.Builder[B, M[B]] = cbf.apply()
        val firstBuilder = fn(head) map { z => builder.+=(z) }
        val eventualBuilder: Future[mutable.Builder[B, M[B]]] = tail.foldLeft(firstBuilder) { (serializedBuilder: Future[mutable.Builder[B, M[B]]], element: A) =>
          serializedBuilder flatMap { (result: mutable.Builder[B, M[B]]) =>
            val f: Future[mutable.Builder[B, M[B]]] = fn(element) map { newElement =>
              result.+=(newElement)
            }
            f
          }
        }
        eventualBuilder map { b => b.result() }
      }
    }
  }

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

  final class FailWithWord private[FutureUtil](bool: Boolean) {
    def failWith(exn: => Throwable): Future[Unit] = if (bool) Future.failed(exn) else Future.unit
  }

  final class FailWithWordFuture private[FutureUtil](val futureCondition: Future[Boolean]) {
    def failWith(exn: => Throwable)(implicit ec: ExecutionContext): Future[Unit] = {
      futureCondition flatMap { condition =>
        if (condition) Future.failed(exn) else Future.unit
      }
    }
  }
}
