package com.lorandszakacs.sg.crawler

import com.lorandszakacs.sg.http.PatienceConfig
import com.lorandszakacs.sg.model._

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
trait ModelAndPhotoSetCrawler {

  def gatherSGNames(limit: Int)(implicit pc: PatienceConfig): Future[List[ModelName]]

  def gatherHopefulNames(limit: Int)(implicit pc: PatienceConfig): Future[List[ModelName]]

  def gatherNewestSets(limit: Int)(implicit pc: PatienceConfig): Future[List[Model]]

  def gatherPhotoSetInformationFor(modelName: ModelName)(implicit pc: PatienceConfig): Future[List[PhotoSet]]

  def gatherNewestModelInformation(limit: Int, lastProcessedIndex: Option[LastProcessedIndex])(implicit pc: PatienceConfig): Future[List[Model]]
}
