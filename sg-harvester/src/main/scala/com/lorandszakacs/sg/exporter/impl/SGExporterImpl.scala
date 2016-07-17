package com.lorandszakacs.sg.exporter.impl

import com.lorandszakacs.sg.Favorites
import com.lorandszakacs.sg.exporter.html.{HTMLGenerator, HtmlSettings, ModelsRootIndex}
import com.lorandszakacs.sg.exporter.indexwriter.HTMLIndexWriter
import com.lorandszakacs.sg.exporter.{ExporterSettings, SGExporter, ModelNotFoundException}
import com.lorandszakacs.sg.model._
import com.lorandszakacs.util.monads.future.FutureUtil._

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 17 Jul 2016
  *
  */
private[exporter] class SGExporterImpl(
  val repo: SGModelRepository,
  val html: HTMLGenerator,
  val fileWriter: HTMLIndexWriter
)(implicit ec: ExecutionContext) extends SGExporter {

  override def writeHTMLIndexOfFavorites(implicit ws: ExporterSettings): Future[Unit] = {
    implicit val htmlSettings = HtmlSettings(
      indexFileName = "index.html",
      rootIndexTitle = "Favorite Suicide Girls"
    )
    for {
      models <- repo.find(Favorites.modelNames)
      favoritesIndex: ModelsRootIndex <- html.createHTMLPageForModels(models)
      _ <- fileWriter.writeModelIndex(favoritesIndex)
    } yield ()
  }

  override def writeHTMLIndexOfAllModels(implicit ws: ExporterSettings): Future[Unit] = {
    implicit val htmlSettings = HtmlSettings(
      indexFileName = "index.html",
      rootIndexTitle = "All Suicide Girls"
    )
    for {
      models <- repo.findAll
      allModelsIndex: ModelsRootIndex <- html.createHTMLPageForModels(models)
      _ <- fileWriter.writeModelIndex(allModelsIndex)
    } yield ()
  }

  def prettyPrint(modelName: ModelName): Future[String] = {
    for {
      model <- repo.find(modelName) map (_.getOrElse(throw ModelNotFoundException(modelName)))
    } yield model match {
      case sg: SuicideGirl => sg.reverseSets.toString
      case h: Hopeful => h.reverseSets.toString
    }
  }
}
