package com.lorandszakacs.sg.sanitizer

import java.nio.file.Paths

import com.lorandszakacs.util.files.FileUtils
import com.lorandszakacs.sg.model.SGModelRepository
import com.lorandszakacs.util.future._
import com.typesafe.scalalogging.StrictLogging

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 14 Jul 2017
  *
  */
final class SGSanitizer(
  private[this] val repo: SGModelRepository
)(implicit val executionContext: ExecutionContext) extends StrictLogging {

  /**
    * The two paths:
    * ``~/suicide-girls/models/favorites/porcelinna/porcelinna_2016-06-11_PEACH_BLOSSOM.html``
    * ``~/suicide-girls/models/favorites/porcelinna/porcelinna_2016-07-19_PEACH_BLOSSOM.html``
    *
    * Differ only in date, if we keep exporting then  you will eventually
    * wind up in a situation that the same set was first published on 2016-06-11, and then on 2016-07-19
    * it gets to the front page of the website, and its publishing date gets also changed.
    * But since the file was in our system for almost a month, it's highly likely that the export
    * contains the old file.
    */
  def detectDuplicateFiles(folderRootPath: String): Future[Set[Set[String]]] = {
    val path = Paths.get(FileUtils.normalizeHomePath(folderRootPath))
    for {
      duplicates <- FileUtils.findPotentialDuplicates(path)
      filtered = duplicates.filterNot { d =>
        d.exists(df => KnownDuplicateSuffixes.exists(_.exists(kds => df.toLowerCase.contains(kds.toLowerCase))))
      }
    } yield filtered
  }

  private lazy val KnownDuplicateSuffixes = Set[Set[String]](
    Set(
      "2007-09-19_AVAST_BEHIND.html",
      "2007-09-04_AVAST_BEHIND.html"
    ),
    Set(
      "2009-09-01_BERRIES.html",
      "2009-09-11_BERRIES.html"
    ),
    Set(
      "2007-10-19_CANYON.html",
      "2007-07-23_CANYON.html"
    ),
    Set(
      "2008-10-25_BACKSTAGE.html",
      "2008-10-22_BACKSTAGE.html"
    ),
    Set(
      "2009-09-03_THE_CLASSIC_RED.html",
      "2009-11-25_THE_CLASSIC_RED.html"
    ),
    Set(
      "2015-08-07_UNTITLED.html",
      "2014-12-22_UNTITLED.html"
    ),
    Set(
      "2008-11-05_FRAGMENTS_OF_A_WOMAN.html",
      "2008-08-27_FRAGMENTS_OF_A_WOMAN.html"
    )
  )
}
