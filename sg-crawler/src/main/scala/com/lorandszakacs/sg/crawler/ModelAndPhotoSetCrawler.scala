package com.lorandszakacs.sg.crawler

import com.lorandszakacs.sg.model.PhotoSet

import scala.concurrent.Future

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
trait ModelAndPhotoSetCrawler {

  def gatherSGNames(limit: Int): Future[List[String]]

  def gatherHopefulNames(limit: Int): Future[List[String]]

  def gatherPhotoSetInformationFor(modelName: String): Future[List[PhotoSet]]
}
