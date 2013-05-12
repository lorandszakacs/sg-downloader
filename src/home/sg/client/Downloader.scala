package home.sg.client

import home.sg.parser.SetAlbum
import java.io.File
import home.sg.parser.PhotoSetInfo
import home.sg.util.IO

/**
 * 0 - no printing whatsoever
 * 1 - only the downloader will print
 * 2 - the http client will also print its status
 */
class LevelOfReporting(level: Int) {
  val silentDownloader = (level < 1)
  val silentClient = (level < 2)
}

private abstract class DownloadStatus(val statusMsg: String)
private case object Incomplete extends DownloadStatus("incomplete: ")
private case object MultiSet extends DownloadStatus("multi-set:  ")
private case object MRSet extends DownloadStatus("mr-set:     ")
private case object Finished extends DownloadStatus("finished")

class Downloader(
  val sgName: String,
  val user: String,
  val password: String,
  levelOfReporting: LevelOfReporting) {

  private def report = if (levelOfReporting.silentDownloader) { (x: Any) => Unit } else { (x: Any) => println(x) }
  private def reportError = if (levelOfReporting.silentDownloader) { (x: Any) => Unit } else { (x: Any) => System.err.println(x) }

  private var sgClient = new SGClient(levelOfReporting.silentClient)

  val setAlbum = new SetAlbum(sgName, sgClient.getSetAlbumPageSource(sgName));

  def download(rootFolder: String) {
    download(rootFolder, (x => true))
  }

  def download(rootFolder: String, filterSetsToDownload: (PhotoSetInfo) => Boolean) {
    val root = IO.createFolder(rootFolder)
    def handleDownload() {
      sgClient.login(user, password)
      val setsToDownload = setAlbum.pinkSets.filter(filterSetsToDownload).sortBy(_.relativeAlbumSaveLocation)

      report("Sets to Download: %d".format(setsToDownload.length))
      setsToDownload foreach (x => report(x.relativeAlbumSaveLocation))

      val downloadStatus = setsToDownload map downloadSet(root)
      report("Finished download")
      logFailureSets(root, downloadStatus zip setsToDownload.map(_.relativeAlbumSaveLocation))
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
          case LoginInvalidUserOrPasswordExn(msg) => reportError(msg)

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

  private def logFailureSets(root: String, setStatus: List[(DownloadStatus, String)]) {
    def statusToString(status: (DownloadStatus, String)): String = status._1.statusMsg + status._2
    val relevantIncomplete = setStatus.filterNot(set => set._1 == MRSet || set._1 == Finished)
    val relevantSetNames = List.concat(relevantIncomplete, setAlbum.mrSets.map(set => (MRSet, set.relativeAlbumSaveLocation))).sortBy(p => p._2) map statusToString

    if (!relevantSetNames.isEmpty) {
      val logFile = IO.concatPath(root, sgName, "incomplete-sets.txt")
      IO.writeToFile(relevantSetNames.mkString("\n").getBytes(), logFile)
    }
    setAlbum.mrSets foreach { set =>
      val setName = IO.concatPath(root, set.relativeAlbumSaveLocation + "_mr")
      IO.createFolder(setName)
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
  private def downloadSet(root: String)(setInfo: PhotoSetInfo): DownloadStatus = {
    val newFolder = IO.concatPath(root, setInfo.relativeAlbumSaveLocation)
    def handleDownload(): DownloadStatus = {
      setInfo.imageDownloadAndSaveLocationPairs match {
        case Some(pairs) => {
          if (IO.exists(newFolder) && !IO.isEmpty(newFolder)) {
            report("\nskipping set: %s   ;already exists".format(setInfo.relativeAlbumSaveLocation))
          } else {
            IO.createFolder(newFolder)
            report("\nDownloading set: %s".format(setInfo.relativeAlbumSaveLocation))
            pairs foreach downloadFile(root)
            //downloadFile will always throw an InexistentFileException
          }
          Finished
        }
        case None => MRSet
      }
    } //end def

    try {
      handleDownload()
    } catch {
      case noFileExn: InexistentFileException => {
        def getImageNumber(fileURL: String) =
          fileURL.takeRight(6).take(2).toInt

        getImageNumber(noFileExn.fileURL) match {
          case 1 => {
            report("  multi-set, skipping")
            MultiSet
          }
          case x if x < 20 =>{
            report("  set is most likely incomplete")
            Incomplete}
          case _ => Finished
        }
      }

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
        report("    %s".format(fileSGSetPath))
      } catch {
        case thw: Throwable => throw new FileWrittingException("Unable to write file: " + URI + thw.getMessage())
      }
    }

    val imgBuffer = sgClient.getSetImage(URI)
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
