package com.lorandszakacs.sg.http

import com.lorandszakacs.sg.URLConversions
import com.lorandszakacs.util.effects._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 11 Jul 2016
  *
  */
package object impl extends URLConversions {
  implicit def convertHTTPIOSchedulerToScheduler(implicit httpSch: HTTPIOScheduler): ExecutionContext = httpSch.scheduler
}
