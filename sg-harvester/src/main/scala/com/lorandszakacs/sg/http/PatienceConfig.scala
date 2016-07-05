package com.lorandszakacs.sg.http

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
case class PatienceConfig(
  throttle: FiniteDuration = 200 millis
)
