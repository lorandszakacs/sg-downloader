package com.lorandszakacs.sg.exporter.indexwriter.impl

import java.io.{File, FileFilter, IOException, PrintWriter}
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import com.lorandszakacs.sg.exporter.indexwriter.{FailedToCreateFolderException, RootFolderCouldNotBeOpenedException, RootFolderFileCouldNotBeDeleted}
import com.lorandszakacs.util.future._
import com.typesafe.scalalogging.StrictLogging

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 17 Jul 2016
  *
  */
private[exporter] object FileUtils extends StrictLogging {

  /**
    * recursively deletes everything in the specified folder
    */
  def cleanFolderOrCreate(fd: Path)(implicit ec: ExecutionContext): Future[Unit] = Future {
    fd.toAbsolutePath.toFile.mkdirs()
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

  def fileMatchInEverythingButDate(s1: String, s2: String): Boolean = {
    if (s1.length != s2.length)
      false
    else {
      val zip = s1.zip(s2)
      val set: Set[Char] = zip.filterNot(p => p._1 == p._2).map(p => Set(p._1, p._2)).toSet.flatten
      val setOfNumber = Set('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
      val intersection = set.diff(setOfNumber)
      set.nonEmpty && intersection.isEmpty
    }
  }

  def findPotentialDuplicates(fd: Path)(implicit ec: ExecutionContext): Future[Set[Set[String]]] = Future {
    def tailsMatch(s1: String, s2: String): Boolean = {
      fileMatchInEverythingButDate(s1, s2)
    }

    val acc = mutable.Set[Set[String]]()
    fd.toAbsolutePath.toFile.mkdirs()
    Files.walkFileTree(
      fd,
      new FileVisitor[Path] {

        override def visitFileFailed(file: Path, exc: IOException): FileVisitResult = {
          if (file.toAbsolutePath == fd.toAbsolutePath) {
            throw RootFolderCouldNotBeOpenedException(fd.toAbsolutePath.toString, exc)
          } else {
            logger.error(s"visit failed @$file", exc)
            FileVisitResult.CONTINUE
          }
        }

        override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
          FileVisitResult.CONTINUE
        }

        override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = {
          val dirFile = dir.toFile
          val onlyFileFilter = new FileFilter {
            override def accept(pathname: File): Boolean = !pathname.isDirectory
          }
          val f: Array[File] = dirFile.listFiles(onlyFileFilter)
          val fileNames = f.map(_.toPath.toAbsolutePath.toString)
          val x: Array[Set[String]] = for {
            f1 <- fileNames
            f2 <- fileNames
            if f1 != f2 && tailsMatch(f1, f2)
          } yield Set(f1, f2)
          val r: Set[String] = x.flatten.toSet
          acc += r
          FileVisitResult.CONTINUE

        }

        override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = {
          FileVisitResult.CONTINUE
        }
      }
    )
    acc.toSet
  }

  def createFolders(fd: Path)(implicit ec: ExecutionContext): Future[Unit] = {
    val f = fd.toAbsolutePath.toFile
    for {
      result <- Future(f.mkdirs())
      _ <- when(!result) failWith FailedToCreateFolderException(f.getAbsolutePath)
    } yield ()
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

  def overwriteFile(fp: Path, content: String)(implicit ec: ExecutionContext): Future[Unit] = Future {
    val file = fp.toAbsolutePath.toFile
    if (file.exists()) {
      file.delete()
    }
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
