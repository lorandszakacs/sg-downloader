package home.sg.client

import java.io.File
import home.sg.util.IO
import home.sg.parser.PhotoAlbumBuilder
import home.sg.parser.PhotoSet

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
  val user: String,
  val password: String,
  levelOfReporting: LevelOfReporting) {

  private val report: (Any => Unit) = if (levelOfReporting.silentDownloader) { (x: Any) => Unit } else { (x: Any) => println(x) }
  private val reportError: (Any => Unit) = if (levelOfReporting.silentDownloader) { (x: Any) => Unit } else { (x: Any) => System.err.println(x) }

  private var sgClient = new SGClient(levelOfReporting.silentClient)

  def download(rootFolder: String) {
    download(rootFolder, (x => true))
  }

  def download(rootFolder: String, filterSetsToDownload: (PhotoSet) => Boolean) {
    val root = IO.createFolder(rootFolder)
    def handleDownload() {
      sgClient.login(user, password)
      report("Fetching: set pages")
      val setsToDownload = PhotoAlbumBuilder.buildSets(sgName, sgClient, report)
      report("Finished gathering all set information")

      report("Sets to Download: %d".format(setsToDownload.length))
      //setsToDownload foreach (s => report(s.relativeSaveLocation))

      val downloadStatus = setsToDownload map downloadSet(root)
      report("Finished download")
    }
    def cleanUpAndRestart(msg: String) {
      reportError("Restarting server because:\n%s".format(msg))
      sgClient.cleanUp()
      sgClient = new SGClient(levelOfReporting.silentClient)
      Thread.sleep(1000)
      handleDownload()
    }

    try {
      handleDownload()
    } catch {
      case sgExn: SGException => {
        sgExn match {
          case FileDownloadException(msg) => reportError("Trouble with file download: " + msg + "\nExiting.")

          case LoginInvalidUserOrPasswordExn(msg) => reportError(msg + "\nExiting")
          case LoginConnectionLostException(msg) => cleanUpAndRestart(msg)
          case LoginUnknownException(msg) => cleanUpAndRestart(msg)

          case HttpClientException(msg) => cleanUpAndRestart(msg)
          case UnknownSGException(msg) => cleanUpAndRestart(msg)
        }
      }
      case ex: Exception => reportError(ex.getMessage() + "\nExiting.")
    } finally {
      sgClient.cleanUp()
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
  private def downloadSet(root: String)(setInfo: PhotoSet) {
    val newFolder = IO.concatPath(root, setInfo.relativeSaveLocation)
    def exists() = IO.exists(newFolder) && !IO.isEmpty(newFolder)

    def handleDownload() {
      if (exists) {
        report("\nskipping set: %s   ;already exists".format(setInfo.relativeSaveLocation))
      } else {
        IO.createFolder(newFolder)
        report("\nDownloading set: %s".format(newFolder))
        setInfo.URLSaveLocationPairs foreach downloadFile(root)
      }
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

  private def downloadFile(root: String)(pair: (String, String)) {
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
