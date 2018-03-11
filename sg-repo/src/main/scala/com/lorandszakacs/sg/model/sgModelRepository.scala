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
  def reindexSGs(names: List[Name]): IO[Unit]

  def reindexHFs(names: List[Name]): IO[Unit]

  def markAsIndexed(newHFs: List[HF], newSGs: List[SG]): IO[Unit]

  def markAsIndexedForNames(newHFs: List[Name], newSGs: List[Name]): IO[Unit]

  def createOrUpdateLastProcessed(l: LastProcessedMarker): IO[Unit]

  def lastProcessedIndex: IO[Option[LastProcessedMarker]]

  def completeIndex: IO[CompleteIndex]

  /**
    * Updates or creates [[SG]], removes the name from [[SGIndex.needsReindexing]]
    */
  def createOrUpdateSGs(sgs: List[SG]): IO[Unit]

  /**
    * Updates or creates [[HF]], removes the name from [[HFIndex.needsReindexing]]
    *
    */
  def createOrUpdateHFs(hfs: List[HF]): IO[Unit]

  /**
    * Returns the Ms which had a on any given day between the two dates given as parameters
    * Where the Ms which are given as a parameter have precedence over the already existing
    * Ms.
    *
    * i.e. Ms parameter is a delta of sorts
    */
  def aggregateBetweenDays(start: LocalDate, end: LocalDate, ms: List[M] = Nil): IO[List[(LocalDate, List[M])]]

  def find(name: Name): IO[Option[M]]

  def find(names: Seq[Name]): IO[List[M]]

  def findAll: IO[List[M]]

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
