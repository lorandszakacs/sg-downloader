package com.lorandszakacs.sg.indexer

import com.lorandszakacs.sg.http.PatienceConfig
import com.lorandszakacs.sg.model.M.ModelFactory
import com.lorandszakacs.sg.model._
import org.joda.time.DateTime

import com.lorandszakacs.util.future._

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
  def gatherSGNames(limit: Int)(implicit pc: PatienceConfig): Future[List[Name]]

  /**
    * Gathers the names of all available [[HF]]s
    */
  def gatherHFNames(limit: Int)(implicit pc: PatienceConfig): Future[List[Name]]

  /**
    *
    * At the time of writing, there are at most 9 sets displayed per such a page.
    *
    * Check:
    * https://www.suicidegirls.com/girls/dwam/photos/view/photosets/
    * To see if this still holds true.
    *
    * @return
    * the [[PhotoSet]]s of the given model.
    * All elements of the list will have: [[PhotoSet.photos.isEmpty]], and [[PhotoSet.url]] will be a full path URL.
    */
  def gatherPhotoSetInformationForModel[T <: M](mf: ModelFactory[T])(modelName: Name)(
    implicit pc:                                    PatienceConfig
  ): Future[T]

  /**
    * Similar to [[gatherPhotoSetInformationForModel]], but with more potential for failure
    */
  def gatherPhotoSetInformationForModel(modelName: Name)(implicit pc: PatienceConfig): Future[M]

  /**
    *
    *
    */
  def gatherAllNewMsAndAllTheirPhotoSets(limit: Int, lastProcessedIndex: Option[LastProcessedMarker])(
    implicit pc:                                PatienceConfig
  ): Future[List[M]]

  final def createLastProcessedIndex(lastModel: M): LastProcessedMarker = {
    LastProcessedMarker(
      timestamp = DateTime.now(),
      photoSet = lastModel.photoSetsNewestFirst.headOption
        .getOrElse(
          throw new AssertionError(
            s"... tried to create last processed index from model ${lastModel.name.name}, but they had no sets"
          )
        )
        .copy(photos = Nil)
    )
  }

}
