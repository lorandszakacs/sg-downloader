package com.lorandszakacs.util.math

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 14 Jul 2017
  *
  */
trait Identifier[Type, IdType] {
  def id(t: Type): IdType
}

object Identifier {

  def apply[Type, IdType](f: Type => IdType) = new Identifier[Type, IdType] {
    override def id(t: Type): IdType = f(t)
  }
}
