package com.lorandszakacs.sg.exporter

import java.nio.file.{Path, Paths}

import com.lorandszakacs.sg.model.{Model, ModelName}
import com.lorandszakacs.util.files.FileUtils
import com.lorandszakacs.util.future._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 17 Jul 2016
  *
  */
trait SGExporter {
  def prettyPrint(modelName: ModelName): Future[String]

  /**
    * Only exports the given [[ModelName]]s. with the data
    * that is available in the system
    *
    * It will recreate the:
    * ./[[ExporterSettings.favoritesRootFolderPath]]/index.html
    * ./[[ExporterSettings.allModelsRootFolderPath]]/index.html
    * Taking into consideration *all* models, and *all favorites*, not just the
    * ones specified.
    *
    * But *all* corresponding model specific folders as specified by the model names,
    * will be created.
    *
    * @return
    */
  def exportHTMLOfOnlyGivenSubsetOfModels(ms: List[ModelName])(implicit ws: ExporterSettings): Future[Unit]

  /**
    * Only exports the given [[Model]]s.
    *
    * It will recreate the:
    * ./[[ExporterSettings.favoritesRootFolderPath]]/index.html
    * ./[[ExporterSettings.allModelsRootFolderPath]]/index.html
    * Taking into consideration *all* models, and *all favorites*, not just the
    * ones specified.
    *
    * But *all* corresponding model specific folders as specified by the model names,
    * will be created.
    *
    * @return
    */
  def exportDeltaHTMLOfModels(ms: List[Model])(implicit ws: ExporterSettings): Future[Unit]

  /**
    * Create a navigable HTML webpage at [[ExporterSettings.favoritesRootFolderPath]]
    *
    */
  def exportHTMLIndexOfFavorites(implicit ws: ExporterSettings): Future[Unit]

  /**
    * Create a navigable HTML webpage at [[ExporterSettings.allModelsRootFolderPath]]
    */
  def exportHTMLIndexOfAllModels(implicit ws: ExporterSettings): Future[Unit]

  /**
    * Creates and HTML webpage at [[ExporterSettings.newestRootFolderPath]] containing
    * the newest sets, grouped per days, for the past ``nrOfDays``.
    */
  def exportLatestForDays(nrOfDays: Int)(implicit ws: ExporterSettings): Future[Unit]

  /**
    * Same as [[exportLatestForDays]] but adds in to the already existing models
    * the delta passed as parameter
    */
  def exportLatestForDaysWithDelta(nrOfDays: Int, delta: List[Model])(implicit ws: ExporterSettings): Future[Unit]
  
}

object ExporterSettings {

  def apply(favoritesRootFolderPath: String, allModelsRootFolderPath: String, newestRootFolderPath: String, rewriteEverything: Boolean): ExporterSettings = {
    new ExporterSettings(
      favoritesRootFolderPath = Paths.get(FileUtils.normalizeHomePath(favoritesRootFolderPath)).toAbsolutePath,
      allModelsRootFolderPath = Paths.get(FileUtils.normalizeHomePath(allModelsRootFolderPath)).toAbsolutePath,
      newestRootFolderPath = Paths.get(FileUtils.normalizeHomePath(newestRootFolderPath)).toAbsolutePath,
      rewriteEverything = rewriteEverything
    )
  }
}


/**
  *
  * @param favoritesRootFolderPath
  * this folder has to exist!
  * @param rewriteEverything
  * if true, then everything in the rootFolderPath will be deleted,
  * then rewritten. if false, then the rootFolderPath has to be empty
  */
final class ExporterSettings private(
  val favoritesRootFolderPath: Path,
  val allModelsRootFolderPath: Path,
  val newestRootFolderPath: Path,
  val rewriteEverything: Boolean
)
