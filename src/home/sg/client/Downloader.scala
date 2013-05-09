package home.sg.client

import home.sg.parser.SetAlbum
import java.io.File
import home.sg.parser.PhotoSetInfo

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
    val root = createRootFolder(rootFolder)

    try {
      sgClient.login(user, password)
      report("AlbumsListing: ")
      setAlbum.pinkSets foreach (x => report(x.relativeAlbumSaveLocation))
      report("-------Starting:")
      setAlbum.pinkSets.filter(filterSetsToDownload) foreach (downloadSet(root))
      report("Finished download")
      logMRSets(root)
    } finally {
      sgClient.shutdown
    }
  }

  private def logMRSets(root: File) {
    if (setAlbum.mrSets.isEmpty)
      Unit
    else {
      val mrLogFile = root.getAbsolutePath() + "/" + sgName + "/" + "mr-sets.txt"
      FileIO.writeToFile(setAlbum.mrSets.map(x => x.relativeAlbumSaveLocation).mkString("\n").toCharArray(), mrLogFile)
    }
  }

  private val magicNumberWeUseToSkipFolderSizes = 4000

  private def downloadSet(root: File)(setInfo: PhotoSetInfo) {
    setInfo.imageDownloadAndSaveLocationPairs match {
      case Some(pairs) => {
        val newFolder = new File(root.getAbsolutePath() + "/" + setInfo.relativeAlbumSaveLocation)
        if (newFolder.exists() && newFolder.getTotalSpace() > magicNumberWeUseToSkipFolderSizes) {
          report("skipping set: %s   ;already exists".format(setInfo.relativeAlbumSaveLocation))
        } else {
          newFolder.mkdirs()
          report("Downloading set: %s".format(setInfo.relativeAlbumSaveLocation))
          pairs foreach downloadFile(root)
        }
        report("================");
      }
      case None => throw new RuntimeException("Trying to download MR set: %s".format(setInfo.relativeAlbumSaveLocation))
    }
  }

  private def downloadFile(root: File)(pair: (String, String)) {
    val URI = pair._1
    val fileSGSetPath = pair._2

    val optionSome = sgClient.getSetImage(URI)
    optionSome match {
      case Some(buff) => {
        val file = createImageFile(root, fileSGSetPath)
        FileIO.writeToFile(buff, file.getAbsolutePath())
        report("   %s".format(fileSGSetPath))
      }
      case None => Unit //report("skipping: %s".format(fileSGSetPath))
    }
  }

  private def createImageFile(root: File, relativeImagePath: String) = {
    val imageFile = new File(root.getCanonicalFile().getAbsolutePath() + "/" + relativeImagePath)
    imageFile.createNewFile()
    if (!imageFile.canWrite()) {
      imageFile.delete();
      throw new RuntimeException("Could not create specified file: %s".format(imageFile))
    } else imageFile
  }

  private def createRootFolder(rootFolder: String) = {
    val folder = new File(rootFolder)
    folder.mkdirs();
    if (!folder.canWrite()) {
      folder.delete();
      throw new RuntimeException("Could not create path specified: %".format(rootFolder))
    } else folder
  }

}