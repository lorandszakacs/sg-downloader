package com.lorandszakacs.sg.model

import scala.concurrent.Future

/**
  *
  * Used to do basic CRUD on the SG information about: SGs, Hopefuls, images, photosets, etc.
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 03 Jul 2016
  *
  */
trait SGModelRepository {

  def createOrUpdateSGIndex(index: SuicideGirlIndex): Future[Unit]

  def writeSG(sg: SuicideGirl): Future[Unit]

  def writeOrUpdate(sg: SuicideGirl): Future[Unit]

  def updateSG(sg: SuicideGirl): Future[Unit]

  def findSG(sgName: String): Future[Option[SuicideGirl]]

  /**
    * It is possible that [[SuicideGirl.photoSets]] -> [[PhotoSet.photos]] is empty
    * and this method returns all such SGs
    */
  def findSGsWithNoPhotoLinks(): Future[Seq[SuicideGirl]]
}
