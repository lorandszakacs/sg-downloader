package com.lorandszakacs.sg.model

import org.joda.time.{DateTime, LocalDate}

import com.lorandszakacs.util.future._

/**
  *
  * Used to do basic CRUD on the SG information about: SGs, Hopefuls, images, photosets, etc.
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 03 Jul 2016
  *
  */
trait SGModelRepository {
  @scala.deprecated("unused", "now")
  def modelsWithZeroPhotoSets: Future[Models]

  def reindexSGs(names: List[ModelName]): Future[Unit]

  def reindexHopefuls(names: List[ModelName]): Future[Unit]

  def markAsIndexed(newHopefuls: List[Hopeful], newSGs: List[SuicideGirl]): Future[Unit]

  def markAsIndexedForNames(newHopefuls: List[ModelName], newSGs: List[ModelName]): Future[Unit]

  def createOrUpdateLastProcessed(l: LastProcessedMarker): Future[Unit]

  def lastProcessedIndex: Future[Option[LastProcessedMarker]]

  def completeModelIndex: Future[CompleteModelIndex]

  /**
    * Updates or creates [[SuicideGirl]], removes the name from [[SuicideGirlIndex.needsReindexing]]
    */
  def createOrUpdateSGs(sgs: List[SuicideGirl]): Future[Unit]

  /**
    * Updates or creates [[Hopeful]], removes the name from [[HopefulIndex.needsReindexing]]
    *
    */
  def createOrUpdateHopefuls(hopefuls: List[Hopeful]): Future[Unit]

  /**
    * Returns the models which had a on any given day between the two dates given as parameters
    * Where the models which are given as a parameter have precedence over the already existing
    * models.
    *
    * i.e. models parameter is a delta of sorts
    */
  def aggregateBetweenDays(start: LocalDate, end: LocalDate, models: List[Model] = Nil): Future[List[(LocalDate, List[Model])]]

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

case class LastProcessedMarker(
  timestamp: DateTime,
  photoSet: PhotoSet
) {
  final def lastPhotoSetID: String = photoSet.id
}
