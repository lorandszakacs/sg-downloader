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

  val sgClient = new Client(user, password)

  val setAlbum = new SetAlbum(sgName, sgClient.getSetAlbumPageSource(sgName));

  def download(rootFolder: String) {
    download(rootFolder, (x => true))
  }

  def download(rootFolder: String, filterSetsToDownload: (PhotoSetInfo) => Boolean) {
    val root = createRootFolder(rootFolder)
    report("STARTING DOWNLOAD: ")
    setAlbum.pinkSets.filter(filterSetsToDownload).map(downloadSet(root))
    report("Finished download")
  }

  private def downloadSet(root: File)(setInfo: PhotoSetInfo) {
    setInfo.imageDownloadAndSaveLocationPairs match {
      case Some(pairs) => {
        new File(root.getAbsolutePath() + "/" + setInfo.relativeAlbumSaveLocation).mkdirs()
        report("Downloading set: %s".format(setInfo.relativeAlbumSaveLocation))
        pairs.foreach(downloadFile(root))
        report("================")
      }
      case None => throw new RuntimeException("Trying to download MR set: %s".format(setInfo.relativeAlbumSaveLocation))
    }

  }

  private def downloadFile(root: File)(pair: (String, String)) {
    val URI = pair._1
    val fileSGSetPath = pair._2

    sgClient.get(URI) match {
      case Some(entity) => {
        val file = createImageFile(root, fileSGSetPath)
        report("   %s".format(URI))
        if (entity.getContentLength() == sgClient.invalidContentLength)
          throw new RuntimeException("LOGIN SESSION EXPIRED, ABORTING!")
        FileIO.writeEntityToFile(entity, file.getAbsolutePath())
      }
      case None => Unit
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