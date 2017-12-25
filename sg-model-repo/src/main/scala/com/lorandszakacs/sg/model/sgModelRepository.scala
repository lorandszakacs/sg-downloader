package com.lorandszakacs.sg.model

import com.lorandszakacs.util.future._
import com.lorandszakacs.util.time._

/**
  *
  * Used to do basic CRUD on the SG information about: SGs, HFs, images, photosets, etc.
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 03 Jul 2016
  *
  */
trait SGAndHFRepository {
  def reindexSGs(names: List[Name]): Future[Unit]

  def reindexHFs(names: List[Name]): Future[Unit]

  def markAsIndexed(newHFs: List[HF], newSGs: List[SG]): Future[Unit]

  def markAsIndexedForNames(newHFs: List[Name], newSGs: List[Name]): Future[Unit]

  def createOrUpdateLastProcessed(l: LastProcessedMarker): Future[Unit]

  def lastProcessedIndex: Future[Option[LastProcessedMarker]]

  def completeIndex: Future[CompleteIndex]

  /**
    * Updates or creates [[SG]], removes the name from [[SGIndex.needsReindexing]]
    */
  def createOrUpdateSGs(sgs: List[SG]): Future[Unit]

  /**
    * Updates or creates [[HF]], removes the name from [[HFIndex.needsReindexing]]
    *
    */
  def createOrUpdateHFs(hfs: List[HF]): Future[Unit]

  /**
    * Returns the Ms which had a on any given day between the two dates given as parameters
    * Where the Ms which are given as a parameter have precedence over the already existing
    * Ms.
    *
    * i.e. Ms parameter is a delta of sorts
    */
  def aggregateBetweenDays(start: LocalDate, end: LocalDate, ms: List[M] = Nil): Future[List[(LocalDate, List[M])]]

  def find(name: Name): Future[Option[M]]

  def find(names: Seq[Name]): Future[List[M]]

  def findAll: Future[List[M]]

}

final case class HFIndex(
  names:           List[Name],
  needsReindexing: List[Name],
  number:          Int
)

final case class SGIndex(
  names:           List[Name],
  needsReindexing: List[Name],
  number:          Int
)

/**
  * Always the sum of [[HFIndex]], and [[SGIndex]]
  */
final case class CompleteIndex(
  names:           List[Name],
  needsReindexing: List[Name],
  number:          Int
)

case class LastProcessedMarker(
  timestamp: DateTime,
  photoSet:  PhotoSet
) {
  final def lastPhotoSetID: String = photoSet.id
}
