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

  val sgClient = new SGClient(silent.silentClient)

  val setAlbum = new SetAlbum(sgName, sgClient.getSetAlbumPageSource(sgName));

  def download(rootFolder: String) {
    download(rootFolder, (x => true))
  }

  def download(rootFolder: String, filterSetsToDownload: (PhotoSetInfo) => Boolean) {
    val root = IO.createFolder(rootFolder)

    try {
      sgClient.login(user, password)
      val setsToDownload = setAlbum.pinkSets.filter(filterSetsToDownload)

      report("Sets to Download: ")
      setsToDownload foreach (x => report(x.relativeAlbumSaveLocation))

      report("-------Starting:")
      setsToDownload foreach (downloadSet(root))
      report("Finished download")
      logMRSets(root)
    } finally {
      sgClient.shutdown
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

  private def downloadSet(root: String)(setInfo: PhotoSetInfo) {
    setInfo.imageDownloadAndSaveLocationPairs match {
      case Some(pairs) => {
        val newFolder = IO.concatPath(root, setInfo.relativeAlbumSaveLocation)
        if (IO.exists(newFolder) && !IO.isEmpty(newFolder)) {
          report("skipping set: %s   ;already exists".format(setInfo.relativeAlbumSaveLocation))
        } else {
          IO.createFolder(newFolder)
          report("Downloading set: %s".format(setInfo.relativeAlbumSaveLocation))
          pairs foreach downloadFile(root)
        }
        report("================");
      }
      case None => throw new RuntimeException("Trying to download MR set: %s".format(setInfo.relativeAlbumSaveLocation))
    }
  }

  private def downloadFile(root: String)(pair: (String, String)) {
    def createImageFile(root: String, relativeImagePath: String) = {
      val imageFile = IO.concatPath(root, relativeImagePath)
      IO.createFile(imageFile)
    }
    val URI = pair._1
    val fileSGSetPath = pair._2

    val optionSome = sgClient.getSetImage(URI)
    optionSome match {
      case Some(buff) => {
        val file = createImageFile(root, fileSGSetPath)
        IO.writeToFile(buff, file)
        report("   %s".format(fileSGSetPath))
      }
      case None => Unit //report("skipping: %s".format(fileSGSetPath))
    }
  }

}