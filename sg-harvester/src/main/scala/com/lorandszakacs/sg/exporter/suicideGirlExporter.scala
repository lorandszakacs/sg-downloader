package com.lorandszakacs.sg.exporter

import java.nio.file.{Path, Paths}

import com.lorandszakacs.sg.model.ModelName
import com.lorandszakacs.util.monads.future.FutureUtil._

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 17 Jul 2016
  *
  */
trait SGExporter {
  def prettyPrint(modelName: ModelName): Future[String]

  /**
    * Only exports the given [[ModelName]]s.
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
  def exportDeltaHTMLIndex(ms: List[ModelName])(implicit ws: ExporterSettings): Future[Unit]

  /**
    * Create a navigable HTML webpage at [[ExporterSettings.favoritesRootFolderPath]]
    *
    */
  def exportHTMLIndexOfFavorites(implicit ws: ExporterSettings): Future[Unit]

  /**
    * Create a navigable HTML webpage at [[ExporterSettings.allModelsRootFolderPath]]
    */
  def exportHTMLIndexOfAllModels(implicit ws: ExporterSettings): Future[Unit]
}

object ExporterSettings {
  private def normalizeHomePath(path: String): String = {
    path.replaceFirst("^~", System.getProperty("user.home"))
  }

  def apply(favoritesRootFolderPath: String, allModelsRootFolderPath: String, rewriteEverything: Boolean): ExporterSettings = {
    new ExporterSettings(
      favoritesRootFolderPath = Paths.get(normalizeHomePath(favoritesRootFolderPath)).toAbsolutePath,
      allModelsRootFolderPath = Paths.get(normalizeHomePath(allModelsRootFolderPath)).toAbsolutePath,
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
  val rewriteEverything: Boolean
)
