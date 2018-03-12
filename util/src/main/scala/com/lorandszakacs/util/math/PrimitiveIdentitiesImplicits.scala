package com.lorandszakacs.util.math

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 12 Jul 2017
  *
  */
trait PrimitiveIdentitiesImplicits {

  implicit val intId: Identity[Int] = new Identity[Int] {
    override def identifiesAs(x: Int, y: Int): Boolean = x == y
  }

  implicit val stringId: Identity[String] = new Identity[String] {
    override def identifiesAs(x: String, y: String): Boolean = x == y
  }

}
