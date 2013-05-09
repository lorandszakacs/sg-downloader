package home.sg.client

import home.sg.parser.SetAlbum
import java.io.File
import home.sg.parser.PhotoSetInfo

class Downloader(
  val sgName: String,
  val user: String,
  val password: String,
  silent: Boolean) {

  def report = if (silent) { (x: Any) => Unit } else { (x: Any) => println(x) }

  val sgClient = new SGClient(true)

  val setAlbum = new SetAlbum(sgName, sgClient.getSetAlbumPageSource(sgName));

  def download(rootFolder: String) {
    download(rootFolder, (x => true))
  }

  def download(rootFolder: String, filterSetsToDownload: (PhotoSetInfo) => Boolean) {
    val root = createRootFolder(rootFolder)

    try {
      sgClient.login(user, password)
      report("Starting:")
      setAlbum.pinkSets.filter(filterSetsToDownload).map(downloadSet(root))
      report("Finished download")
    } finally {
      sgClient.shutdown
    }
  }

  private def downloadSet(root: File)(setInfo: PhotoSetInfo) {
    setInfo.imageDownloadAndSaveLocationPairs match {
      case Some(pairs) => {
        val newFolder = new File(root.getAbsolutePath() + "/" + setInfo.relativeAlbumSaveLocation)
        if (newFolder.exists() && newFolder.getTotalSpace() > 4000) {
          report("skipping set: %s   ;already exists".format(setInfo.relativeAlbumSaveLocation))
        } else {
          newFolder.mkdir()
          report("Downloading set: %s".format(setInfo.relativeAlbumSaveLocation))
          pairs.foreach(downloadFile(root))
        }
        report("================");
      }
      case None => throw new RuntimeException("Trying to download MR set: %s".format(setInfo.relativeAlbumSaveLocation))
    }
  }

  private def downloadFile(root: File)(pair: (String, String)) {
    val URI = pair._1
    val fileSGSetPath = pair._2

    val optionSome = sgClient.get(URI)
    optionSome match {
      case Some(buff) => {
        val file = createImageFile(root, fileSGSetPath)
        FileIO.writeToFile(buff, file.getAbsolutePath())
        report("   %s".format(fileSGSetPath))
      }
      case None => report("failed: %s".format(fileSGSetPath))
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