package home.sg.client

import scala.annotation.elidable
import scala.annotation.elidable.ASSERTION

import home.sg.parser.PhotoAlbumBuilder
import home.sg.parser.PhotoSetHeader
import home.sg.util.IO

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
    download(rootFolder, (_ => true))
  }

  def download(rootFolder: String, toDownload: (PhotoSetHeader) => Boolean) {
    val root = IO.createFolder(rootFolder)
    val headers = PhotoAlbumBuilder.buildSetHeaders(sgName, sgClient) partition (h => IO.existsAndEmptyFolder(root, h.relativeSaveLocation))
    reportSkips(headers._1)
    val novelHeaders = headers._2
    val setsToDownload = novelHeaders filter toDownload

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
  private def downloadSet(root: String, a: SGClient)(photoSetHeader: PhotoSetHeader) {
    val newFolder = IO.concatPath(root, photoSetHeader.relativeSaveLocation)

    def handleDownload() {
      val photoSet = PhotoAlbumBuilder.buildPhotoSet(sgName, photoSetHeader, sgClient, report)
      IO.createFolder(newFolder)
      report("\nDownloading set: %s".format(newFolder))
      photoSet.URLSaveLocationPairs foreach downloadFile(root, a)
    }

    def handleRename() {
      val allFiles = IO.listFiles(IO.concatPath(root, photoSetHeader.sgName))
      val temp = allFiles.filter(s => s.contains(photoSetHeader.title))
      assume(temp.length == 1, "Trying to perform a rename where it is not necessary: %s".format(photoSetHeader.relativeSaveLocation))
      val oldFolder = newFolder.replace(photoSetHeader.fileSystemSetTitle, temp.head)
      report("\nSet: %s; was previously in MR so the date differs, renaming it to: %s".format(oldFolder, newFolder))
      IO.rename(oldFolder, newFolder)
    }

    def requiresRename(): Boolean = {
      val allFiles = IO.listFiles(IO.concatPath(root, photoSetHeader.sgName))
      val wasMR = !(allFiles.filter(s => s.contains(photoSetHeader.title)).isEmpty)
      wasMR
    }

    try {
      if (requiresRename)
        handleRename()
      else
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
