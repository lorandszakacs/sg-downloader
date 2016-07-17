package com.lorandszakacs.sg.exporter.indexwriter.impl

import java.io.{IOException, PrintWriter}
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import com.lorandszakacs.sg.exporter.indexwriter.{FailedToCreateFolderException, RootFolderCouldNotBeOpenedException, RootFolderFileCouldNotBeDeleted}
import com.lorandszakacs.util.monads.future.FutureUtil._
import com.typesafe.scalalogging.StrictLogging

import scala.util.{Failure, Success, Try}

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 17 Jul 2016
  *
  */
private[indexwriter] object FileUtils extends StrictLogging {

  /**
    * recursively deletes everything in the specified folder
    */
  def cleanFolder(fd: Path)(implicit ec: ExecutionContext): Future[Unit] = Future {
    Files.walkFileTree(
      fd,
      new FileVisitor[Path] {
        override def visitFileFailed(file: Path, exc: IOException): FileVisitResult = {
          if (file.toAbsolutePath == fd.toAbsolutePath) {
            throw RootFolderCouldNotBeOpenedException(fd.toAbsolutePath.toString, exc)
          } else {
            FileVisitResult.CONTINUE
          }
        }

        override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
          if (Files.isDirectory(file)) {
            //in this case, the folder will be deleted in the postVisitDirectory, once the folder is deleted.
            FileVisitResult.CONTINUE
          } else {
            Try(Files.delete(file)) match {
              case Failure(exception) =>
                throw RootFolderFileCouldNotBeDeleted(file.toAbsolutePath.toString, exception)
              case Success(_) => ()
            }
            FileVisitResult.CONTINUE
          }
        }

        override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = {
          FileVisitResult.CONTINUE

        }

        override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = {
          if (dir.toAbsolutePath != fd.toAbsolutePath) {
            Files.deleteIfExists(dir)
            FileVisitResult.CONTINUE
          } else {
            FileVisitResult.CONTINUE
          }
        }
      }
    )
    logger.info(s"successfully cleaned: ${fd.toAbsolutePath}")
  }

  def createFolders(fd: Path)(implicit ec: ExecutionContext): Future[Unit] = {
    val f = fd.toAbsolutePath.toFile
    if (f.mkdirs()) {
      UnitFuture
    } else {
      Future.failed(FailedToCreateFolderException(f.getAbsolutePath))
    }
  }


  /**
    * Does NOT overwrite file!
    *
    */
  def writeFile(fp: Path, content: String)(implicit ec: ExecutionContext): Future[Unit] = Future {
    val writer = new PrintWriter(fp.toAbsolutePath.toFile)
    Try(writer.write(content)) match {
      case Success(_) =>
        writer.close()
      case Failure(exception) =>
        writer.close()
        throw exception
    }
  }
}
