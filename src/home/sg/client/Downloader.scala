package home.sg.client

import home.sg.parser.SetAlbum
import java.io.File
import home.sg.parser.PhotoSetInfo
import home.sg.util.IO

class LevelOfReporting(level: Int) {
  val silentDownloader = (level < 1)
  val silentClient = (level < 2)
}

class Downloader(
  val sgName: String,
  val user: String,
  val password: String,
  silent: LevelOfReporting) {

  def report = if (silent.silentDownloader) { (x: Any) => Unit } else { (x: Any) => println(x) }

  private var sgClient = new SGClient(silent.silentClient)

  val setAlbum = new SetAlbum(sgName, sgClient.getSetAlbumPageSource(sgName));

  def download(rootFolder: String) {
    download(rootFolder, (x => true))
  }

  def download(rootFolder: String, filterSetsToDownload: (PhotoSetInfo) => Boolean) {
    val root = IO.createFolder(rootFolder)
    def handleDownload() {
      sgClient.login(user, password)
      val setsToDownload = setAlbum.pinkSets.filter(filterSetsToDownload)

      report("Sets to Download: ")
      setsToDownload foreach (x => report(x.relativeAlbumSaveLocation))

      report("-------Starting:")
      setsToDownload foreach (downloadSet(root))
      report("Finished download")
      logMRSets(root)
    }
    def cleanUpAndRestart(msg: String) {
      System.err.println("Restarting server because:\n%s".format(msg))
      sgClient.cleanUp()
      sgClient = new SGClient(silent.silentClient)
      Thread.sleep(1000)
      handleDownload()
    }

    //here goes nothing
    try {
      handleDownload()
    } catch {
      case sgExn: SGException => {
        sgExn match {
          case LoginInvalidUserOrPasswordExn(msg) => System.err.println(msg)
          
          case LoginConnectionLostException(msg) => cleanUpAndRestart(msg)
          case LoginUnknownException(msg) => cleanUpAndRestart(msg)
          case FileDownloadException(msg) => cleanUpAndRestart(msg)
          case HttpClientException(msg) => cleanUpAndRestart(msg)
          case UnknownSGException(msg) => cleanUpAndRestart(msg)
        }
      }
      case ex: Exception => cleanUpAndRestart(ex.getMessage())
    } finally {
      sgClient.cleanUp()
    }
  }

  private def logMRSets(root: String) {
    if (setAlbum.mrSets.isEmpty)
      Unit
    else {
      val mrLogFile = IO.concatPath(root, sgName, "mr-sets.txt")
      IO.writeToFile(setAlbum.mrSets.map(x => x.relativeAlbumSaveLocation).mkString("\n").getBytes(), mrLogFile)
      setAlbum.mrSets foreach { set =>
        val setName = IO.concatPath(root, set.relativeAlbumSaveLocation + "_mr")
        IO.createFolder(setName)
      }
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
  private def downloadSet(root: String)(setInfo: PhotoSetInfo) {
    val newFolder = IO.concatPath(root, setInfo.relativeAlbumSaveLocation)
    def handleDownload() = {
      setInfo.imageDownloadAndSaveLocationPairs match {
        case Some(pairs) => {
          if (IO.exists(newFolder) && !IO.isEmpty(newFolder)) {
            report("skipping set: %s   ;already exists".format(setInfo.relativeAlbumSaveLocation))
          } else {
            IO.createFolder(newFolder)
            report("Downloading set: %s".format(setInfo.relativeAlbumSaveLocation))
            pairs foreach downloadFile(root)
          }
          report("================");
        }
        case None => throw new DownloadingMRSetException("Trying to download MR set: %s".format(setInfo.relativeAlbumSaveLocation))
      }
    }; /////
    try {
      handleDownload()
    } catch {
      //all other exceptions are handled by the above context because they require
      //serious re-downloading
      case noFileExn: InexistentFileException => Unit

      //maybe let this one propagate up and actually remove the set?
      case mrSetExn: DownloadingMRSetException => Unit

      //if it is any other exception then we delete this set, so we can try again
      case thw: Throwable => {
        IO.deleteFolder(newFolder)
        throw thw
      }
    }
  }

  private def downloadFile(root: String)(pair: (String, String)) {
    val URI = pair._1
    val fileSGSetPath = pair._2

    def createImageFile(root: String, relativeImagePath: String) = {
      val imageFile = IO.concatPath(root, relativeImagePath)
      IO.createFile(imageFile)
    }

    def handleWritting(buff: Array[Byte]) {
      try {
        val file = createImageFile(root, fileSGSetPath)
        IO.writeToFile(buff, file)
        report("   %s".format(fileSGSetPath))
      } catch {
        case thw: Throwable => throw new FileWrittingException("Unable to write file: " + URI + thw.getMessage())
      }
    }

    val imgBuffer = sgClient.getSetImage(URI)
    try {
      handleWritting(imgBuffer)
    } catch {
      case thw: Throwable => handleWritting(imgBuffer)
    }
  }

}