package com.lorandszakacs.sg.crawler

import com.lorandszakacs.sg.http.PatienceConfig
import com.lorandszakacs.sg.model._
import org.joda.time.DateTime

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

  def gatherPhotoSetInformationFor(modelName: ModelName)(implicit pc: PatienceConfig): Future[List[PhotoSet]]

  def gatherNewestModelInformation(limit: Int, lastProcessedIndex: Option[LastProcessedMarker])(implicit pc: PatienceConfig): Future[List[Model]]

  final def createLastProcessedIndex(lastModel: Model): LastProcessedMarker = lastModel match {
    case h: Hopeful =>
      LastProcessedHopeful(
        timestamp = DateTime.now(),
        hopeful = h
      )
    case sg: SuicideGirl =>
      LastProcessedSG(
        timestamp = DateTime.now(),
        suicidegirl = sg
      )
  }
}
