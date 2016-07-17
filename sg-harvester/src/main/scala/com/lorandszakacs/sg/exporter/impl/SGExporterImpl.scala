package com.lorandszakacs.sg.exporter.impl

import com.lorandszakacs.sg.Favorites
import com.lorandszakacs.sg.exporter.html.{HTMLGenerator, HtmlSettings, ModelsRootIndex}
import com.lorandszakacs.sg.exporter.indexwriter.{HTMLIndexWriter, WriterSettings}
import com.lorandszakacs.sg.exporter.{ExporterSettings, ModelNotFoundException, SGExporter}
import com.lorandszakacs.sg.model._
import com.lorandszakacs.util.monads.future.FutureUtil._
import com.typesafe.scalalogging.StrictLogging

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
)(implicit ec: ExecutionContext) extends SGExporter with StrictLogging {

  private val FavoritesHtmlSettings = HtmlSettings(
    indexFileName = "index.html",
    rootIndexTitle = "Favorite Suicide Girls"
  )

  private def favoritesWriterSettings(implicit es: ExporterSettings) = WriterSettings(
    rootFolder = es.favoritesRootFolderPath,
    rewriteEverything = es.rewriteEverything
  )

  private def allWriterSettings(implicit es: ExporterSettings) = WriterSettings(
    rootFolder = es.allModelsRootFolderPath,
    rewriteEverything = es.rewriteEverything
  )

  private val AllHtmlSettings = HtmlSettings(
    indexFileName = "index.html",
    rootIndexTitle = "All Suicide Girls"
  )

  override def exportDeltaHTMLIndex(ms: List[ModelName])(implicit ws: ExporterSettings): Future[Unit] = {
    def updateFavorites(deltaFavorites: List[Model])(implicit ws: ExporterSettings): Future[Unit] = {
      if (deltaFavorites.nonEmpty) {
        for {
          favoritesIndexDelta <- html.createHTMLPageForModels(deltaFavorites)(FavoritesHtmlSettings)
          _ <- fileWriter.writeRootModelIndex(favoritesIndexDelta)(favoritesWriterSettings)
          completeFavoriteRootIndex <- html.createRootIndex(Favorites.modelNames)(FavoritesHtmlSettings)
          _ <- fileWriter.rewriteRootIndexFile(completeFavoriteRootIndex)(favoritesWriterSettings)
        } yield {
          logger.info(s"-- successfully updated DELTA favorites index ${deltaFavorites.length}: @ ${completeFavoriteRootIndex.relativePathAndName}")
        }
      } else {
        logger.info("-- no delta for favorite models.")
        UnitFuture
      }
    }

    def updateAll(delta: List[Model])(implicit ws: ExporterSettings) = {
      if (delta.nonEmpty) {
        for {
          completeIndex: CompleteModelIndex <- repo.completeModelIndex
          allIndexDelta <- html.createHTMLPageForModels(delta)(AllHtmlSettings)
          _ <- fileWriter.writeRootModelIndex(allIndexDelta)(allWriterSettings)
          allRootIndex <- html.createRootIndex(completeIndex.names)(AllHtmlSettings)
          _ <- fileWriter.rewriteRootIndexFile(allRootIndex)(allWriterSettings)
        } yield {
          logger.info(s"--- successfully updated DELTA all model index of: ${delta.length}")
        }
      } else {
        logger.info("-- no delta for normal models.")
        UnitFuture
      }
    }

    for {
      models <- repo.find(ms)
      favorites: List[Model] = models.filter(m => Favorites.modelNames.contains(m.name))

      _ <- updateFavorites(favorites)
      _ <- updateAll(models)
    } yield ()
  }

  override def exportHTMLIndexOfFavorites(implicit ws: ExporterSettings): Future[Unit] = {
    for {
      models <- repo.find(Favorites.modelNames)
      favoritesIndex: ModelsRootIndex <- html.createHTMLPageForModels(models)(FavoritesHtmlSettings)
      _ <- fileWriter.writeRootModelIndex(favoritesIndex)(favoritesWriterSettings)
    } yield ()
  }

  override def exportHTMLIndexOfAllModels(implicit ws: ExporterSettings): Future[Unit] = {
    for {
      models <- repo.findAll
      allModelsIndex: ModelsRootIndex <- html.createHTMLPageForModels(models)(AllHtmlSettings)
      _ <- fileWriter.writeRootModelIndex(allModelsIndex)(allWriterSettings)
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
