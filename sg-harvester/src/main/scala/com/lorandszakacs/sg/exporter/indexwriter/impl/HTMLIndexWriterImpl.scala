package com.lorandszakacs.sg.exporter.indexwriter.impl

import java.nio.file.Path

import com.lorandszakacs.sg.exporter._
import com.lorandszakacs.sg.exporter.html._
import com.lorandszakacs.sg.exporter.indexwriter.HTMLIndexWriter
import com.lorandszakacs.util.monads.future.FutureUtil._
import com.typesafe.scalalogging.StrictLogging

import scala.util.control.NonFatal

/**
  *
  * For simplicity's sake ``./`` denotes the path [[ExporterSettings.rootFolderPath]]
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 17 Jul 2016
  *
  */
private[indexwriter] class HTMLIndexWriterImpl()
  (implicit val ec: ExecutionContext) extends HTMLIndexWriter with StrictLogging {

  override def writeModelIndex(index: ModelsRootIndex)(implicit ws: ExporterSettings): Future[Unit] = {
    for {
      _ <- (if (ws.rewriteEverything) FileUtils.cleanFolder(ws.rootFolderPath) else UnitFuture) recover {
        case NonFatal(e) =>
          logger.error(s"failed to clean root folder: ${e.getMessage}", e)
          throw e
      }
      _ <- writeRootIndexFile(index)

    } yield ()
  }

  /**
    * This writes:[[ModelsRootIndex.html]] to ``./index.html`` on the disk
    */
  private def writeRootIndexFile(index: ModelsRootIndex)(implicit ws: ExporterSettings): Future[Unit] = {
    val path = ws.rootFolderPath.resolve(index.indexFileName)
    for {
      _ <- Future.traverse(index.models) { model =>
        writeModelIndex(model)
      }
      _ = logger.info(s"finished writing all #${index.models.length} models @ ${ws.rootFolderPath}")
      _ <- FileUtils.writeFile(path, index.html.value)
    } yield ()
  }

  /**
    * It will create a folder ./[[ModelIndex.name]]/, and write everything inside that folder
    * This writes:[[ModelIndex.modelIndexHtml]] to ``./[[ModelIndex.modelIndexHtmlFileName]]`` on the disk
    */
  private def writeModelIndex(m: ModelIndex)(implicit ws: ExporterSettings): Future[Unit] = {
    def writeModelPhotoSetIndex(modelFolderPath: Path)(ps: PhotoSetIndex): Future[Unit] = {
      val psPath = modelFolderPath.resolve(ps.htmlFileName)
      FileUtils.writeFile(psPath, ps.html.value) map { _ =>
        logger.debug(s"successfully wrote file: $psPath")
      }
    }
    val modelFolderPath = ws.rootFolderPath.resolve(m.name.name).toAbsolutePath
    val indexPath = ws.rootFolderPath.resolve(m.modelIndexHtmlFileName).toAbsolutePath
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


