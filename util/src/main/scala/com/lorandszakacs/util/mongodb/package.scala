package com.lorandszakacs.util

import com.lorandszakacs.util.effects._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Jun 2017
  *
  */
package object mongodb extends MongoQueries with MongoDBTypes {
  implicit def convertDBIOSchedulerToScheduler(implicit httpSch: DBIOScheduler): Scheduler = httpSch.scheduler
}
