package com.lorandszakacs.sg.harvester

import com.lorandszakacs.sg.model._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
trait SGHarvester {

  /**
    * Fetches all [[SuicideGirl]] names from the website, and updates them in the
    * local repository.
    *
    * @return
    * a list of all the names of the SGs in the index
    */
  def updateSGIndex(timeout: FiniteDuration = 1 hour): Future[List[String]]

  /**
    * Fetches all [[Hopeful]] names from the website, and updates them in the
    * local repository.
    *
    * @return
    * a list of all the names of the SGs in the index
    */
  def updateHopefulIndex(timeout: FiniteDuration = 1 hour): Future[List[String]]

  /**
    * Updates [[SuicideGirl.photoSets]] with [[com.lorandszakacs.sg.model.PhotoSet]]s with all information
    * except the image links.
    */
  def gatherPhotoSetInformationForSGsInIndex(timeout: FiniteDuration = 1 hour): Future[List[SuicideGirl]]
}
