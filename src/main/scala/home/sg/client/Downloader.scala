package home.sg.client

import java.io.File
import home.sg.util.IO
import home.sg.parser.PhotoAlbumBuilder
import home.sg.parser.PhotoSet
import home.sg.parser.PhotoSetHeader

/**
 * 0 - no printing whatsoever
 * 1 - only the downloader will print
 * 2 - the http client will print its status
 */
class LevelOfReporting(level: Int) {
  val silentDownloader = (level < 1)
  val silentClient = (level < 2)
}

class Downloader(
  val sgName: String,
  val sgClient: SGClient,
  levelOfReporting: LevelOfReporting) {

  private val report: (Any => Unit) = if (levelOfReporting.silentDownloader) { (x: Any) => Unit } else { (x: Any) => println(x) }
  private val reportError: (Any => Unit) = if (levelOfReporting.silentDownloader) { (x: Any) => Unit } else { (x: Any) => System.err.println(x) }

  def download(rootFolder: String) {
    download(rootFolder, (x => true))
  }

  def download(rootFolder: String, toDownload: (PhotoSet) => Boolean) {
    val root = IO.createFolder(rootFolder)
    val headers = PhotoAlbumBuilder.computeSetHeaders(sgName, sgClient) partition (h => IO.existsAndEmptyFolder(root, h.relativeSaveLocation))
    reportSkips(headers._1)
    val novelHeaders = headers._2

    val allSets = PhotoAlbumBuilder.buildPhotoSets(sgName, novelHeaders, sgClient, report)
    val setsToDownload = allSets filter toDownload

    report("Number of sets to download for %s: %d".format(sgName, setsToDownload.length))
    setsToDownload foreach downloadSet(root, sgClient)
  }

  private def reportSkips(existingSets: List[PhotoSetHeader]) {
    if (existingSets.length > 0) {
      report("=======\nskipping:")
      existingSets foreach { h => report("  %s".format(h.relativeSaveLocation)) }
      report("=======")
    }
  }
  /**
   * Will download all the images contained in the SetInfo and save
   * them at the location root + somePath from the setInfo.
   *
   * If an error occurs during download it deletes the folder
   * and rethrows the exception
   *
   * @param root
   * @param setInfo
   */
  private def downloadSet(root: String, a: SGClient)(photoSet: PhotoSet) {
    val newFolder = IO.concatPath(root, photoSet.relativeSaveLocation)
    def handleDownload() {
      IO.createFolder(newFolder)
      report("\nDownloading set: %s".format(newFolder))
      photoSet.URLSaveLocationPairs foreach downloadFile(root, a)
    }

    try {
      handleDownload()
    } catch {
      //if it is any other exception then we delete this set, so we can try again
      case thw: Throwable => {
        IO.deleteFolder(newFolder)
        throw thw
      }
    }
  }

  private def downloadFile(root: String, a: SGClient)(pair: (String, String)) {
    val URL = pair._1
    val relativeFilePath = pair._2

    def createImageFile(root: String, relativeImagePath: String) = {
      val imageFile = IO.concatPath(root, relativeImagePath)
      IO.createFile(imageFile)
    }

    def handleWritting(buff: Array[Byte]) {
      try {
        val file = createImageFile(root, relativeFilePath)
        IO.writeToFile(buff, file)
        report("    %s".format(relativeFilePath))
      } catch {
        case thw: Throwable =>
          throw new FileWrittingException("Unable to write file: " + URL + thw.getMessage())
      }
    }

    val imgBuffer = sgClient.getSetImage(URL)
    try {
      handleWritting(imgBuffer)
    } catch {
      case thw: Throwable => {
        reportError("    download failed, retrying")
        handleWritting(imgBuffer)
      }
    }
  }

}
