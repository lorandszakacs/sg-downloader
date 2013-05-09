package home.sg.client

import scala.util.control.Breaks._
import java.net.URL
import java.net.URLConnection
import java.io.IOException
import java.io.InputStream
import java.io.BufferedInputStream
import java.io.FileOutputStream
import org.apache.commons.codec.binary.Base64
import org.apache.http.HttpEntity
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.File

object FileIO {

  def writeToFile(data: Array[Byte], fileName: String) {
    val out = new FileOutputStream(fileName);
    out.write(data);
    out.flush();
    out.close();
  }

  def writeToFile(data: Array[Char], fileName: String) {
    val out = new BufferedWriter(new FileWriter(new File(fileName)));
    out.write(data);
    out.flush();
    out.close();
  }

  private def getByteArrayFromInputStream(raw: InputStream, contentLength: Int) = {
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
  private def writeInputStreamToFile(is: InputStream, length: Int, fileName: String) {
    val data = getByteArrayFromInputStream(is, length)
    writeToFile(data, fileName)
  }

  private def writeEntityToFile(entity: HttpEntity, fileName: String) {
    writeInputStreamToFile(entity.getContent(), entity.getContentLength().toInt, fileName)
    entity.consumeContent();
  }

}