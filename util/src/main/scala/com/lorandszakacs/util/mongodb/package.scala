package com.lorandszakacs.util

import com.lorandszakacs.util.effects._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 26 Jun 2017
  *
  */
package object mongodb extends MongoQueries with MongoDBTypes {

  @inline implicit def convertDBIOSchedulerToScheduler(implicit dbIOScheduler: DBIOScheduler): Scheduler =
    dbIOScheduler.scheduler
}
