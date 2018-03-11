package com.lorandszakacs.sg.indexer

import com.lorandszakacs.sg.http.PatienceConfig
import com.lorandszakacs.sg.model.M.MFactory
import com.lorandszakacs.sg.model._
import org.joda.time.DateTime

import com.lorandszakacs.util.effects._

/**
  * Represents the first stage of the pipeline of processing
  *
  * "indexing" can be done without authentication. It implies gathering all
  * model names, and their associated photosets.
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
trait SGIndexer {

  /**
    * Gathers the names of all available [[SG]]s
    */
  def gatherSGNames(limit: Int)(implicit pc: PatienceConfig): Task[List[Name]]

  /**
    * Gathers the names of all available [[HF]]s
    */
  def gatherHFNames(limit: Int)(implicit pc: PatienceConfig): Task[List[Name]]

  /**
    *
    * At the time of writing, there are at most 9 sets displayed per such a page.
    *
    * Check:
    * $domain/girls/dwam/photos/view/photosets/
    * To see if this still holds true.
    *
    * @return
    * the [[PhotoSet]]s of the given model.
    * All elements of the list will have: [[PhotoSet.photos.isEmpty]], and [[PhotoSet.url]] will be a full path URL.
    */
  def gatherPhotoSetInformationForM[T <: M](mf: MFactory[T])(name: Name)(implicit pc: PatienceConfig): Task[T]

  /**
    * Similar to [[gatherPhotoSetInformationForName]], but with more potential for failure
    */
  def gatherPhotoSetInformationForName(name: Name)(implicit pc: PatienceConfig): Task[M]

  /**
    *
    *
    */
  def gatherAllNewMsAndAllTheirPhotoSets(limit: Int, lastProcessedIndex: Option[LastProcessedMarker])(
    implicit pc:                                PatienceConfig
  ): Task[List[M]]

  final def createLastProcessedIndex(lastM: M): LastProcessedMarker = {
    LastProcessedMarker(
      timestamp = DateTime.now(),
      photoSet = lastM.photoSetsNewestFirst.headOption
        .getOrElse(
          throw new AssertionError(
            s"... tried to create last processed index from model ${lastM.name.name}, but they had no sets"
          )
        )
        .copy(photos = Nil)
    )
  }

}
