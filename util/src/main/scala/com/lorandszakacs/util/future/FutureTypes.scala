package com.lorandszakacs.util.future

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Jun 2017
  *
  */
trait FutureTypes {
  /**
    * convenience aliases, so that you don't have to import this package and [[scala.concurrent.Future]]
    * at the same time
    */
  val Future: concurrent.Future.type = concurrent.Future
  type Future[+T] = concurrent.Future[T]
  val ExecutionContext: concurrent.ExecutionContext.type = concurrent.ExecutionContext
  type ExecutionContext = concurrent.ExecutionContext

  val UnitFunction: Any => Unit = { _ => () }

  val FutureUnitFunction: Any => Future[Unit] = { _ => Future.unit }
}
