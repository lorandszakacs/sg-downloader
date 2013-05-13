package home.sg.util

import scala.util.control.Breaks._
import java.io.IOException
import java.io.InputStream
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.io.BufferedWriter
import java.io.FileWriter
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import java.io.File

object IO {

  def createFolder(folderPath: String): String = {
    val folder = FileUtils.getFile(folderPath)
    folder.mkdirs()

    if (!folder.canWrite()) {
      folder.delete();
      throw new RuntimeException("Could not create path specified: %".format(folder))
    } else folder.getAbsolutePath()
  }

  def listFiles(folderPath: String): List[String] = {
    val folder = FileUtils.getFile(folderPath)
    folder.list().toList
  }

  def createFile(filePath: String): String = {
    val newFile = FileUtils.getFile(filePath)
    assume(newFile.createNewFile(), "could not create file: %".format(filePath))
    newFile.getCanonicalPath()
  }

  def concatPath(basePath: String, onePath: String, toConcat: String*): String = {
    val tempPath = FilenameUtils.concat(basePath, onePath)
    def recursivelyConcatenate(files: Seq[String]): String = {
      if (files.tail.isEmpty)
        FilenameUtils.concat(tempPath, files.head)
      else
        FilenameUtils.concat(recursivelyConcatenate(files.tail), files.head)
    }

    if (toConcat.isEmpty)
      tempPath
    else
      recursivelyConcatenate(toConcat.reverse)
  }

  def exists(folderPath: String): Boolean = {
    FileUtils.getFile(folderPath).exists()
  }

  def isEmpty(folderPath: String): Boolean = {
    FileUtils.getFile(folderPath).list().isEmpty
  }

  def deleteFolder(folder: String) {
    FileUtils.deleteDirectory(FileUtils.getFile(folder))
  }

  def deleteFolder(folder: File) {
    FileUtils.deleteDirectory(folder)
  }

  def writeToFile(data: Array[Byte], fileName: String) {
    val out = new FileOutputStream(fileName);
    out.write(data);
    out.flush();
    out.close();
  }

  def writeToFile(data: Array[Char], fileName: String) {
    val out = new BufferedWriter(new FileWriter(FileUtils.getFile(fileName)));
    out.write(data);
    out.flush();
    out.close();
  }

  def getByteArrayFromInputStream(raw: InputStream, contentLength: Int) = {
    val in = new BufferedInputStream(raw);
    val data = new Array[Byte](contentLength)

    var bytesRead = 0;
    var offset = 0;
    breakable {
      while (offset < contentLength) {
        bytesRead = in.read(data, offset, data.length - offset);
        if (bytesRead == -1)
          break;
        offset += bytesRead;
      }
    }
    in.close();

    if (offset != contentLength) {
      throw new IOException("Only read " + offset + " bytes; Expected " + contentLength + " bytes");
    }
    data
  }

}