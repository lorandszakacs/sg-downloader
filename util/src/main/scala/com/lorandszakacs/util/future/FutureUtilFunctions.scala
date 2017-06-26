package com.lorandszakacs.util.future

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Jun 2017
  *
  */
trait FutureUtilFunctions {
  this: FutureTypes =>

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

}
