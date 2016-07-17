package com.lorandszakacs.sg.exporter.indexwriter.impl

import java.nio.file.Path

import com.lorandszakacs.sg.exporter._
import com.lorandszakacs.sg.exporter.html._
import com.lorandszakacs.sg.exporter.indexwriter.{HTMLIndexWriter, WriterSettings}
import com.lorandszakacs.util.monads.future.FutureUtil._
import com.typesafe.scalalogging.StrictLogging

import scala.util.control.NonFatal

/**
  *
  * For simplicity's sake ``./`` denotes the path to either [[ExporterSettings.favoritesRootFolderPath]],
  * or [[ExporterSettings.allModelsRootFolderPath]], depending on context
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 17 Jul 2016
  *
  */
private[indexwriter] class HTMLIndexWriterImpl()
  (implicit val ec: ExecutionContext) extends HTMLIndexWriter with StrictLogging {

  override def writeRootModelIndex(index: ModelsRootIndex)(implicit ws: WriterSettings): Future[Unit] = {
    for {
      _ <- (if (ws.rewriteEverything) FileUtils.cleanFolder(ws.rootFolder) else UnitFuture) recover {
        case NonFatal(e) =>
          logger.error(s"failed to clean root folder: ${e.getMessage}", e)
          throw e
      }
      _ <- writeRootIndexFile(index)

    } yield ()
  }

  override def rewriteRootIndexFile(indexFile: Html)(implicit ws: WriterSettings): Future[Unit] = {
    val p = ws.rootFolder.resolve(indexFile.relativePathAndName).toAbsolutePath
    FileUtils.overwriteFile(p, indexFile.value) map { _ =>
      logger.info(s"rewrote root index file @ $p")
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
      _ <- FileUtils.writeFile(path, rootIndex.html.value)
    } yield ()
  }

  /**
    * It will create a folder ./[[ModelIndex.name]]/, and write everything inside that folder
    * This writes:[[ModelIndex.modelIndexHtml]] to ``./[[ModelIndex.modelIndexHtml.relativePathAndName]]`` on the disk
    */
  private def writeModelIndex(rootIndex: ModelsRootIndex)(m: ModelIndex)(implicit ws: WriterSettings): Future[Unit] = {
    def writeModelPhotoSetIndex(modelFolderPath: Path)(ps: PhotoSetIndex)(implicit ws: WriterSettings): Future[Unit] = {
      val psPath = modelFolderPath.resolve(ps.html.relativePathAndName)
      FileUtils.writeFile(psPath, ps.html.value) map { _ =>
        logger.debug(s"successfully wrote file: $psPath")
      }
    }
    val modelFolderPath = ws.rootFolder.resolve(m.name.name).toAbsolutePath
    val indexPath = ws.rootFolder.resolve(m.modelIndexHtml.relativePathAndName).toAbsolutePath
    for {
      _ <- FileUtils.createFolders(modelFolderPath)
      _ <- Future.serialize(m.photoSets) { ps =>
        writeModelPhotoSetIndex(modelFolderPath)(ps)
      }
      _ <- FileUtils.writeFile(indexPath, m.modelIndexHtml.value)
    } yield {
      logger.info(s"wrote index for model: ${m.name.name} @ $modelFolderPath")
    }
  }

}


