package com.lorandszakacs.sg.crawler

import com.lorandszakacs.sg.http.PatienceConfig
import com.lorandszakacs.sg.model.PhotoSet

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
trait ModelAndPhotoSetCrawler {

  def gatherSGNames(limit: Int)(implicit pc: PatienceConfig): Future[List[String]]

  def gatherHopefulNames(limit: Int)(implicit pc: PatienceConfig): Future[List[String]]

  def gatherPhotoSetInformationFor(modelName: String)(implicit pc: PatienceConfig): Future[List[PhotoSet]]
}
