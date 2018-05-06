package com.lorandszakacs.sg.model

import com.lorandszakacs.util.effects._
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
  def reindexSGs(names: List[Name]): Task[Unit]

  def reindexHFs(names: List[Name]): Task[Unit]

  def markAsIndexed(newHFs: List[HF], newSGs: List[SG]): Task[Unit]

  def markAsIndexedForNames(newHFs: List[Name], newSGs: List[Name]): Task[Unit]

  def createOrUpdateLastProcessed(l: LastProcessedMarker): Task[Unit]

  def lastProcessedIndex: Task[Option[LastProcessedMarker]]

  def completeIndex: Task[CompleteIndex]

  /**
    * Updates or creates [[SG]], removes the name from [[SGIndex.needsReindexing]]
    */
  def createOrUpdateSGs(sgs: List[SG]): Task[Unit]

  /**
    * Updates or creates [[HF]], removes the name from [[HFIndex.needsReindexing]]
    *
    */
  def createOrUpdateHFs(hfs: List[HF]): Task[Unit]

  /**
    * Returns the Ms which had a on any given day between the two dates given as parameters
    * Where the Ms which are given as a parameter have precedence over the already existing
    * Ms.
    *
    * i.e. Ms parameter is a delta of sorts
    */
  def aggregateBetweenDays(start: LocalDate, end: LocalDate, ms: List[M] = Nil): Task[List[(LocalDate, List[M])]]

  def find(name: Name): Task[Option[M]]

  def find(names: Seq[Name]): Task[List[M]]

  def findAll: Task[List[M]]

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
  timestamp: Instant,
  photoSet:  PhotoSet
) {
  final def lastPhotoSetID: String = photoSet.id
}
