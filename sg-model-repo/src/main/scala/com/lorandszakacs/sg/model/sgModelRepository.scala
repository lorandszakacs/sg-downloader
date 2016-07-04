package com.lorandszakacs.sg.model

import org.joda.time.DateTime

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

  def createOrUpdateHopefulIndex(index: HopefulIndex): Future[Unit]

  def createOrUpdateLastProcessed(l: LastProcessedIndex): Future[Unit]
}


final case class HopefulIndex(
  names: List[ModelName],
  number: Int
)

final case class SuicideGirlIndex(
  names: List[ModelName],
  number: Int
)

sealed trait LastProcessedIndex {
  def url: String

  def timestamp: DateTime
}

case class LastProcessedSG(
  url: String,
  timestamp: DateTime,
  suicidegirl: SuicideGirl
) extends LastProcessedIndex

case class LastProcessedHopeful(
  url: String,
  timestamp: DateTime,
  hopeful: Hopeful
) extends LastProcessedIndex