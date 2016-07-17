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
  def modelsWithZeroPhotoSets: Future[(List[SuicideGirl], List[Hopeful])]

  def reindexSGs(names: List[ModelName]): Future[Unit]

  def reindexHopefuls(names: List[ModelName]): Future[Unit]

  def updateIndexes(newHopefuls: List[Hopeful], newSGs: List[SuicideGirl]): Future[Unit]

  def updateIndexesForNames(newHopefuls: List[ModelName], newSGs: List[ModelName]): Future[Unit]

  /**
    * Removes all specified [[ModelName]]s from the appropriate indexes, removes all [[Model]]
    * entries. Updates the [[CleanedUpModelsIndex]] with the specified models.
    */
  def cleanUpModels(sgs: List[ModelName], hopefuls: List[ModelName]): Future[Unit]

  def createOrUpdateLastProcessed(l: LastProcessedMarker): Future[Unit]

  def lastProcessedIndex: Future[Option[LastProcessedMarker]]

  def suicideGirlIndex: Future[SuicideGirlIndex]

  def hopefulIndex: Future[HopefulIndex]

  def completeModelIndex: Future[CompleteModelIndex]

  /**
    * Updates or creates [[SuicideGirl]], removes the name from [[SuicideGirlIndex.needsReindexing]]
    */
  def createOrUpdateSG(sg: SuicideGirl): Future[Unit]

  /**
    * Updates or creates [[Hopeful]], removes the name from [[HopefulIndex.needsReindexing]]
    *
    */
  def createOrUpdateHopeful(hopeful: Hopeful): Future[Unit]

  def find(modelName: ModelName): Future[Option[Model]]

  def find(modelNames: Seq[ModelName]): Future[List[Model]]

  def findAll: Future[List[Model]]

}


final case class HopefulIndex(
  names: List[ModelName],
  needsReindexing: List[ModelName],
  number: Int
)

final case class SuicideGirlIndex(
  names: List[ModelName],
  needsReindexing: List[ModelName],
  number: Int
)

/**
  * Always the sum of [[HopefulIndex]], and [[SuicideGirlIndex]]
  */
final case class CompleteModelIndex(
  names: List[ModelName],
  needsReindexing: List[ModelName],
  number: Int
)

final case class CleanedUpModelsIndex(
  suicideGirls: List[ModelName],
  hopefuls: List[ModelName]
)

sealed trait LastProcessedMarker {
  def timestamp: DateTime

  def model: Model

  final def lastPhotoSetID = {
    val ph: PhotoSet = model.photoSets.headOption.getOrElse(throw new AssertionError("... tried to get lastPhotoSet, of ProccessedIndex, but it did not exist"))
    ph.id
  }
}

case class LastProcessedSG(
  timestamp: DateTime,
  suicidegirl: SuicideGirl
) extends LastProcessedMarker {
  override def model: Model = suicidegirl
}

case class LastProcessedHopeful(
  timestamp: DateTime,
  hopeful: Hopeful
) extends LastProcessedMarker {
  override def model: Model = hopeful
}