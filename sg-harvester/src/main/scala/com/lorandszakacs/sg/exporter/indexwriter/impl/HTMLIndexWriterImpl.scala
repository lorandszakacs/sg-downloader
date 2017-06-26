package com.lorandszakacs.sg.exporter.indexwriter.impl


import com.lorandszakacs.sg.exporter.html._
import com.lorandszakacs.sg.exporter.indexwriter.{HTMLIndexWriter, WriterSettings}
import com.lorandszakacs.util.monads.future.FutureUtil._
import com.typesafe.scalalogging.StrictLogging

import scala.util.control.NonFatal

/**
  *
  * For simplicity's sake ``./`` denotes the path to either
  * [[com.lorandszakacs.sg.exporter.ExporterSettings.favoritesRootFolderPath]],
  * or [[com.lorandszakacs.sg.exporter.ExporterSettings.allModelsRootFolderPath]], depending on context
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 17 Jul 2016
  *
  */
private[indexwriter] class HTMLIndexWriterImpl()
  (implicit val ec: ExecutionContext) extends HTMLIndexWriter with StrictLogging {

  override def writeRootModelIndex(index: ModelsRootIndex)(implicit ws: WriterSettings): Future[Unit] = {
    for {
      _ <- (if (ws.rewriteEverything) FileUtils.cleanFolderOrCreate(ws.rootFolder) else Future.unit) recover {
        case NonFatal(e) =>
          logger.error(s"failed to clean root folder: ${e.getMessage}", e)
          throw e
      }
      _ <- writeRootIndexFile(index)

    } yield ()
  }

  override def rewriteRootIndexFile(indexFile: Html)(implicit ws: WriterSettings): Future[Unit] = {
    val p = ws.rootFolder.resolve(indexFile.relativePathAndName).toAbsolutePath
    FileUtils.overwriteFile(p, indexFile.content) map { _ =>
      logger.info(s"rewrote root index file @ $p")
    }
  }

  override def rewriteNewestModelPage(newestFile: Html)(implicit ws: WriterSettings): Future[Unit] = {
    val p = ws.rootFolder.resolve(newestFile.relativePathAndName).toAbsolutePath
    FileUtils.overwriteFile(p, newestFile.content) map { _ =>
      logger.info(s"rewrote newest sets file @ $p")
    }
  }

  /**
    * This writes:[[ModelsRootIndex.html]] to ``./index.html`` on the disk
    */
  private def writeRootIndexFile(rootIndex: ModelsRootIndex)(implicit ws: WriterSettings): Future[Unit] = {
    val path = ws.rootFolder.resolve(rootIndex.html.relativePathAndName)
    for {
      _: List[Unit] <- Future.traverse(rootIndex.models) { m: ModelIndex => writeModelIndex(rootIndex)(m) }
      _ = logger.info(s"finished writing all #${rootIndex.models.length} models @ ${ws.rootFolder}")
      _ <- FileUtils.writeFile(path, rootIndex.html.content)
    } yield ()
  }

  /**
    * It will create a folder ./[[ModelIndex.name]]/, and write everything inside that folder
    * This writes:[[ModelIndex.html]] to ``./[[ModelIndex.html.relativePathAndName]]`` on the disk
    */
  private def writeModelIndex(rootIndex: ModelsRootIndex)(m: ModelIndex)(implicit ws: WriterSettings): Future[Unit] = {
    def writeModelPhotoSetIndex(ps: PhotoSetIndex)(implicit ws: WriterSettings): Future[Unit] = {
      val psPath = ws.rootFolder.resolve(ps.html.relativePathAndName)
      logger.debug(s"attempting to write file: $psPath")
      FileUtils.writeFile(psPath, ps.html.content) map { _ =>
        logger.debug(s"successfully wrote file: $psPath")
      }
    }

    val modelFolderPath = ws.rootFolder.resolve(m.name.name).toAbsolutePath
    val indexPath = ws.rootFolder.resolve(m.html.relativePathAndName).toAbsolutePath
    for {
      _ <- FileUtils.createFolders(modelFolderPath)
      _ <- Future.serialize(m.photoSets) { ps =>
        writeModelPhotoSetIndex(ps)
      }
      _ <- FileUtils.writeFile(indexPath, m.html.content)
    } yield {
      logger.info(s"wrote entire model entry @ $indexPath")
    }
  }

}


