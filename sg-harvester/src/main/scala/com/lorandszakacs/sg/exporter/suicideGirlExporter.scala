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

  def writeHTMLIndexOfFavorites(implicit ws: ExporterSettings): Future[Unit]

  def writeHTMLIndexOfAllModels(implicit ws: ExporterSettings): Future[Unit]
}

object ExporterSettings {
  def apply(rootFolderPath: String, rewriteEverything: Boolean): ExporterSettings = {
    val normalizedPathString = rootFolderPath.replaceFirst("^~", System.getProperty("user.home"))
    new ExporterSettings(
      rootFolderPath = Paths.get(normalizedPathString),
      rewriteEverything = rewriteEverything
    )
  }
}


/**
  *
  * @param rootFolderPath
  * this folder has to exist!
  * @param rewriteEverything
  * if true, then everything in the rootFolderPath will be deleted,
  * then rewritten. if false, then the rootFolderPath has to be empty
  */
final class ExporterSettings private(
  val rootFolderPath: Path,
  val rewriteEverything: Boolean
)
