package home.sg.client

import home.sg.parser.SetAlbum
import java.io.File
import home.sg.parser.PhotoSetInfo

class Downloader(
  val sgName: String,
  user: String,
  password: String,
  silent: Boolean) {

  def report = if (silent) { (x: Any) => Unit } else { (x: Any) => println(x) }

  def sgClient = new Client(user, password)

  val setAlbum = new SetAlbum(sgName, sgClient.getSetAlbumPageSource(sgName));

  def download(rootFolder: String) = {
    val root = createRootFolder(rootFolder)

  }

  private def downloadSet(root: File)(setInfo: PhotoSetInfo) = {
    setInfo.imageDownloadAndSaveLocationPairs match {
      case Some(pairs) => {
        report("Downloading set: %s".format(setInfo.relativeAlbumSaveLocation))
        pairs.map(downloadFile(root))
        report("================")
      }
      case None => throw new RuntimeException("Trying to download MR set: %s".format(setInfo.relativeAlbumSaveLocation))
    }

  }

  private def downloadFile(root: File)(pair: (String, String)): Int = {
    val URI = pair._1
    val fileSGSetPath = pair._2

    sgClient.get(URI) match {
      case Some(entity) => {
        val file = createImageFile(root, fileSGSetPath)
        report("   %s".format(URI))
        FileIO.writeEntityToFile(entity, file.getAbsolutePath())
        1
      }
      case None => 0
    }
  }

  private def createImageFile(root: File, relativeImagePath: String) = {
    val imageFile = new File(root.getCanonicalFile().getAbsolutePath() + "/" + relativeImagePath)
    imageFile.mkdirs()
    if (!imageFile.canWrite()) {
      imageFile.delete();
      throw new RuntimeException("Could not create specified file: %".format(imageFile))
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