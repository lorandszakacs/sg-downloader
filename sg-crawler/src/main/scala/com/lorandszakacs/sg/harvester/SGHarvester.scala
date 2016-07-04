package com.lorandszakacs.sg.harvester

import com.lorandszakacs.sg.http.PatienceConfig
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
  def updateSGIndex(maxNrOfSGs: Int)(implicit pc: PatienceConfig): Future[List[ModelName]]

  /**
    * Fetches all [[Hopeful]] names from the website, and updates them in the
    * local repository.
    *
    * @return
    * a list of all the names of the SGs in the index
    */
  def updateHopefulIndex(maxNrOfHopefuls: Int)(implicit pc: PatienceConfig): Future[List[ModelName]]

  /**
    * Gathers all, or the max number of [[PhotoSet]] from the last harvesting.
    *
    * It harvests the following page:
    * https://www.suicidegirls.com/photos/
    */
  def gatherNewestPhotosAndUpdateIndex(maxNrOfSets: Int)(implicit pc: PatienceConfig): Future[List[Model]]

  /**
    * Updates [[SuicideGirl.photoSets]] with [[com.lorandszakacs.sg.model.PhotoSet]]s with all information
    * except the image links.
    */
  def gatherPhotoSetInformationForSGsInIndex(implicit pc: PatienceConfig): Future[List[SuicideGirl]]
}