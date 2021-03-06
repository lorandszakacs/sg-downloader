package com.lorandszakacs.sg.exporter.indexwriter.impl

import com.lorandszakacs.util.effects._
import com.lorandszakacs.sg.exporter.html._
import com.lorandszakacs.sg.exporter.indexwriter.{HTMLIndexWriter, WriterSettings}
import com.lorandszakacs.util.files.FileUtils
import com.lorandszakacs.util.logger._

/**
  *
  * For simplicity's sake ``./`` denotes the path to either
  * [[com.lorandszakacs.sg.exporter.ExporterSettings.favoritesRootFolderPath]],
  * or [[com.lorandszakacs.sg.exporter.ExporterSettings.allMsRootFolderPath]], depending on context
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 17 Jul 2016
  *
  */
private[indexwriter] class HTMLIndexWriterImpl() extends HTMLIndexWriter {
  implicit private val logger: Logger[IO] = Logger.getLogger[IO]

  override def writeRootMIndex(index: MRootIndex)(implicit ws: WriterSettings): IO[Unit] = {
    for {
      _ <- (if (ws.rewriteEverything) FileUtils.cleanFolderOrCreate(ws.rootFolder) else IO.unit).recoverWith {
        case NonFatal(e) => logger.error(e)(s"failed to clean root folder: ${e.getMessage}") >> IO.raiseError(e)
      }
      _ <- writeRootIndexFile(index)

    } yield ()
  }

  override def rewriteRootIndexFile(indexFile: Html)(implicit ws: WriterSettings): IO[Unit] = {
    val p = ws.rootFolder.resolve(indexFile.relativePathAndName).toAbsolutePath
    FileUtils.overwriteFile(p, indexFile.content) >> logger.info(s"rewrote root index file @ $p")
  }

  override def rewriteNewestMPage(newestFile: Html)(implicit ws: WriterSettings): IO[Unit] = {
    val p = ws.rootFolder.resolve(newestFile.relativePathAndName).toAbsolutePath
    FileUtils.overwriteFile(p, newestFile.content) >> logger.info(s"rewrote newest sets file @ $p")
  }

  /**
    * This writes:[[MRootIndex.html]] to ``./index.html`` on the disk
    */
  private def writeRootIndexFile(rootIndex: MRootIndex)(implicit ws: WriterSettings): IO[Unit] = {
    val path = ws.rootFolder.resolve(rootIndex.html.relativePathAndName)
    for {
      _ <- rootIndex.ms.traverse(writeMIndex)
      _ <- logger.info(s"finished writing all #${rootIndex.ms.length} Ms @ ${ws.rootFolder}")
      _ <- FileUtils.writeFile(path, rootIndex.html.content)
    } yield ()
  }

  /**
    * It will create a folder ./[[MIndex.name]]/, and write everything inside that folder
    * This writes:[[MIndex.html]] to ``./[[MIndex.html.relativePathAndName]]`` on the disk
    */
  private def writeMIndex(m: MIndex)(implicit ws: WriterSettings): IO[Unit] = {
    def writeMPhotoSetIndex(ps: PhotoSetIndex)(implicit ws: WriterSettings): IO[Unit] = {
      val psPath = ws.rootFolder.resolve(ps.html.relativePathAndName)
      logger.debug(s"attempting to write file: $psPath") >>
        FileUtils.writeFile(psPath, ps.html.content) >>
        logger.debug(s"successfully wrote file: $psPath")
    }

    val mFolderPath = ws.rootFolder.resolve(m.name.name).toAbsolutePath
    val indexPath   = ws.rootFolder.resolve(m.html.relativePathAndName).toAbsolutePath
    for {
      _ <- FileUtils.createFolders(mFolderPath)
      _ <- m.photoSets.traverse { ps =>
        writeMPhotoSetIndex(ps)
      }
      _ <- FileUtils.writeFile(indexPath, m.html.content)
      _ <- logger.info(s"wrote entire M entry @ $indexPath")
    } yield ()
  }

}
