package com.lorandszakacs.sg.exporter.indexwriter.impl

import com.lorandszakacs.util.effects._
import com.lorandszakacs.sg.exporter.html._
import com.lorandszakacs.sg.exporter.indexwriter.{HTMLIndexWriter, WriterSettings}
import com.lorandszakacs.util.files.FileUtils
import org.iolog4s.Logger

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
  implicit private val logger: Logger[Task] = Logger.create[Task]

  override def writeRootMIndex(index: MRootIndex)(implicit ws: WriterSettings): Task[Unit] = {
    for {
      _ <- (if (ws.rewriteEverything) FileUtils.cleanFolderOrCreate(ws.rootFolder) else Task.unit) recoverWith {
            case NonFatal(e) => logger.error(e)(s"failed to clean root folder: ${e.getMessage}") >> Task.raiseError(e)
          }
      _ <- writeRootIndexFile(index)

    } yield ()
  }

  override def rewriteRootIndexFile(indexFile: Html)(implicit ws: WriterSettings): Task[Unit] = {
    val p = ws.rootFolder.resolve(indexFile.relativePathAndName).toAbsolutePath
    FileUtils.overwriteFile(p, indexFile.content) >> logger.info(s"rewrote root index file @ $p")
  }

  override def rewriteNewestMPage(newestFile: Html)(implicit ws: WriterSettings): Task[Unit] = {
    val p = ws.rootFolder.resolve(newestFile.relativePathAndName).toAbsolutePath
    FileUtils.overwriteFile(p, newestFile.content) >> logger.info(s"rewrote newest sets file @ $p")
  }

  /**
    * This writes:[[MRootIndex.html]] to ``./index.html`` on the disk
    */
  private def writeRootIndexFile(rootIndex: MRootIndex)(implicit ws: WriterSettings): Task[Unit] = {
    val path = ws.rootFolder.resolve(rootIndex.html.relativePathAndName)
    for {
      _ <- Task.traverse(rootIndex.ms)(writeMIndex)
      _ <- logger.info(s"finished writing all #${rootIndex.ms.length} Ms @ ${ws.rootFolder}")
      _ <- FileUtils.writeFile(path, rootIndex.html.content)
    } yield ()
  }

  /**
    * It will create a folder ./[[MIndex.name]]/, and write everything inside that folder
    * This writes:[[MIndex.html]] to ``./[[MIndex.html.relativePathAndName]]`` on the disk
    */
  private def writeMIndex(m: MIndex)(implicit ws: WriterSettings): Task[Unit] = {
    def writeMPhotoSetIndex(ps: PhotoSetIndex)(implicit ws: WriterSettings): Task[Unit] = {
      val psPath = ws.rootFolder.resolve(ps.html.relativePathAndName)
      logger.debug(s"attempting to write file: $psPath") >>
        FileUtils.writeFile(psPath, ps.html.content) >>
        logger.debug(s"successfully wrote file: $psPath")
    }

    val mFolderPath = ws.rootFolder.resolve(m.name.name).toAbsolutePath
    val indexPath   = ws.rootFolder.resolve(m.html.relativePathAndName).toAbsolutePath
    for {
      _ <- FileUtils.createFolders(mFolderPath)
      _ <- Task.serialize(m.photoSets) { ps =>
            writeMPhotoSetIndex(ps)
          }
      _ <- FileUtils.writeFile(indexPath, m.html.content)
      _ <- logger.info(s"wrote entire M entry @ $indexPath")
    } yield ()
  }

}
