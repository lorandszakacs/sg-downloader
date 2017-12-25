package com.lorandszakacs.util.math

/**
  *
  * This is meant to be a shallower version of an equality type class.
  * Basically meant to compare things by "id". Useful when you have deep
  * case class hierarchies, and don't want to do deep equality
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 12 Jul 2017
  *
  */
trait Identity[T] {
  def identifiesAs(x: T, y: T): Boolean
}

object Identity {

  def apply[T](f: (T, T) => Boolean) = new Identity[T] {
    override def identifiesAs(x: T, y: T): Boolean = f(x, y)
  }
}
