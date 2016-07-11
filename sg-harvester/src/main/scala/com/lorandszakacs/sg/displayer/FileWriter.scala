package com.lorandszakacs.sg.displayer

import java.io.{File, PrintWriter}
import java.nio.file.{Path, Paths}

import com.lorandszakacs.util.monads.future.FutureUtil.ExecutionContext
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.Future

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 11 Jul 2016
  *
  */
object FileWriter extends StrictLogging {

  private val prefix = "/home/lorand/suicide-girls/html"

  def writeFiles(modelDisplay: ModelDisplay)(implicit ec: ExecutionContext): Future[Unit] = {
    Future {
      val folder = Paths.get(prefix, modelDisplay.name.name).toFile
      if (!folder.exists()) {
        folder.mkdir()
        modelDisplay.photoSets.foreach { set =>
          val path = Paths.get(prefix, modelDisplay.name.name, set.name)
          val file = path.toFile
          logger.info(s"attempting to write file: ${file.getAbsolutePath}")
          writeFile(file, set.html)
        }
        val indexPath = Paths.get(prefix, modelDisplay.name.name, "index.html")
        val indexFile = indexPath.toFile
        logger.info(s"attempting to write index file: ${indexFile.getAbsoluteFile}")
        writeFile(indexFile, modelDisplay.photoSetIndexHtml)
      } else {
        logger.error(s"output folder: ${folder.getAbsolutePath} already exists. Please delete, and try again.")
      }
    }
  }

  def writeIndex(indexFileContent: String)(implicit ec: ExecutionContext): Future[Unit] = {
    Future {
      val path = Paths.get(prefix, "index.html")
      val file = path.toFile
      writeFile(file, indexFileContent)
    }
  }

  def writeFile(file: File, content: String): Unit = {
    val writer = new PrintWriter(file)
    writer.write(content)
    writer.close()
  }

}
