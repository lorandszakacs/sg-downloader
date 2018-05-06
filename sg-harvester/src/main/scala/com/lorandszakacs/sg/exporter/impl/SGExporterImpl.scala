package com.lorandszakacs.sg.exporter.impl

import com.lorandszakacs.util.time._
import com.lorandszakacs.sg.Favorites
import com.lorandszakacs.sg.exporter.html._
import com.lorandszakacs.sg.exporter.indexwriter.{HTMLIndexWriter, WriterSettings}
import com.lorandszakacs.sg.exporter._
import com.lorandszakacs.sg.model._
import com.lorandszakacs.util.effects._
import org.iolog4s.Logger

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 17 Jul 2016
  *
  */
private[exporter] class SGExporterImpl(
  val repo:       SGAndHFRepository,
  val html:       HTMLGenerator,
  val fileWriter: HTMLIndexWriter
) extends SGExporter {

  implicit private val logger: Logger[Task] = Logger.create[Task]

  private val FavoritesHtmlSettings = HtmlSettings(
    indexFileName  = "index.html",
    rootIndexTitle = "Favorite SGs"
  )

  private def favoritesWriterSettings(implicit es: ExporterSettings) = WriterSettings(
    rootFolder        = es.favoritesRootFolderPath,
    rewriteEverything = es.rewriteEverything
  )

  private def allWriterSettings(implicit es: ExporterSettings) = WriterSettings(
    rootFolder        = es.allMsRootFolderPath,
    rewriteEverything = es.rewriteEverything
  )

  private def newestWriterSettings(implicit es: ExporterSettings) = WriterSettings(
    rootFolder        = es.newestRootFolderPath,
    rewriteEverything = es.rewriteEverything
  )

  private val AllHtmlSettings = HtmlSettings(
    indexFileName  = "index.html",
    rootIndexTitle = "All SGs"
  )

  private def updateFavoritesHTML(deltaFavorites: List[M])(implicit ws: ExporterSettings): Task[Unit] = {
    if (deltaFavorites.nonEmpty) {
      for {
        favoritesIndexDelta       <- html.createHTMLPageForMs(deltaFavorites)(FavoritesHtmlSettings)
        _                         <- fileWriter.writeRootMIndex(favoritesIndexDelta)(favoritesWriterSettings)
        completeFavoriteRootIndex <- html.createRootIndex(Favorites.names)(FavoritesHtmlSettings)
        _                         <- fileWriter.rewriteRootIndexFile(completeFavoriteRootIndex)(favoritesWriterSettings)
        _ <- logger.info(
              s"-- successfully updated DELTA favorites index ${deltaFavorites.length}: @ ${completeFavoriteRootIndex.relativePathAndName}"
            )
      } yield ()
    }
    else {
      logger.info("-- no delta for favorite")
    }
  }

  private def updateAllHTML(delta: List[M])(implicit ws: ExporterSettings): Task[Unit] = {
    if (delta.nonEmpty) {
      for {
        completeIndex <- repo.completeIndex
        allIndexDelta <- html.createHTMLPageForMs(delta)(AllHtmlSettings)
        _             <- fileWriter.writeRootMIndex(allIndexDelta)(allWriterSettings)
        allRootIndex  <- html.createRootIndex(completeIndex.names)(AllHtmlSettings)
        _             <- fileWriter.rewriteRootIndexFile(allRootIndex)(allWriterSettings)
        _             <- logger.info(s"--- successfully updated DELTA all M index of: ${delta.length}")
      } yield ()
    }
    else {
      logger.info("-- no delta for normal ms")
    }
  }

  override def exportHTMLOfOnlyGivenSubsetOfMs(names: List[Name])(implicit ws: ExporterSettings): Task[Unit] = {
    for {
      ms <- repo.find(names)
      favorites: List[M] = ms.filter(m => Favorites.names.contains(m.name))

      _ <- updateFavoritesHTML(favorites)
      _ <- updateAllHTML(ms)
    } yield ()
  }

  override def exportDeltaHTMLOfMs(ms: List[M])(implicit ws: ExporterSettings): Task[Unit] = {
    val favorites: List[M] = ms.filter(m => Favorites.names.contains(m.name))
    for {
      _ <- updateFavoritesHTML(favorites)
      _ <- updateAllHTML(ms)
    } yield ()
  }

  override def exportHTMLIndexOfFavorites(implicit ws: ExporterSettings): Task[Unit] = {
    for {
      ms       <- repo.find(Favorites.names)
      favIndex <- html.createHTMLPageForMs(ms)(FavoritesHtmlSettings)
      _        <- fileWriter.writeRootMIndex(favIndex)(favoritesWriterSettings)
    } yield ()
  }

  override def exportHTMLIndexOfAllMs(implicit ws: ExporterSettings): Task[Unit] = {
    for {
      ms         <- repo.findAll
      allMsIndex <- html.createHTMLPageForMs(ms)(AllHtmlSettings)
      _          <- fileWriter.writeRootMIndex(allMsIndex)(allWriterSettings)
    } yield ()
  }

  override def exportLatestForDaysWithDelta(
    nrOfDays:  Int,
    delta:     List[M],
    favorites: Set[Name]
  )(
    implicit ws: ExporterSettings
  ): Task[Unit] = {
    val today     = LocalDate.unsafeToday()
    val inThePast = today.minusDays(nrOfDays.toLong)
    for {
      ms <- repo.aggregateBetweenDays(inThePast, today, delta)
      sortedLatestToEarliest = ms.sortBy(_._1).reverse
      newestMsPage <- html.createNewestPage(sortedLatestToEarliest, favorites)
      _            <- fileWriter.rewriteNewestMPage(newestMsPage)(newestWriterSettings)
    } yield ()
  }

  override def exportLatestForDays(
    nrOfDays:  Int,
    favorites: Set[Name]
  )(
    implicit ws: ExporterSettings
  ): Task[Unit] = {
    val today     = LocalDate.unsafeToday()
    val inThePast = today.minusDays(nrOfDays.toLong)
    for {
      ms <- repo.aggregateBetweenDays(inThePast, today)
      sortedLatestToEarliest = ms.sortBy(_._1).reverse
      newestMsPage <- html.createNewestPage(sortedLatestToEarliest, favorites)
      _            <- fileWriter.rewriteNewestMPage(newestMsPage)(newestWriterSettings)
    } yield ()
  }

  override def prettyPrint(name: Name): Task[String] = {
    for {
      m <- repo.find(name) map (_.getOrElse(throw NameNotFoundException(name)))
    } yield m.setsByNewestFirst.toString
  }

}
