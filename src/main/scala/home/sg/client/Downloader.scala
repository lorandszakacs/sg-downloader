package home.sg.client

import scala.annotation.elidable
import scala.annotation.elidable.ASSERTION
import home.sg.util.IO
import home.sg.parser.html.PhotoSetHeader
import home.sg.parser.html.PhotoAlbumBuilder

class Downloader(
  val sgName: String,
  val sgClient: SGClient) {

  private val report: (Any => Unit) = (x: Any) => println(x)
  private val reportError: (Any => Unit) = (x: Any) => System.err.println(x)

  def download(rootFolder: String) {
    val root = IO.createFolder(rootFolder)
    val optionHeaders = PhotoAlbumBuilder.buildSetHeaders(sgName, sgClient);

    optionHeaders match {
      case None => report("%s: is a hopeful, skipping".format(sgName))
      case Some(possibleHeaders) => {
        val headers = possibleHeaders partition (h => IO.existsAndEmptyFolder(root, h.relativeSaveLocation))
        reportSkips(headers._1)
        val novelHeaders = headers._2
        val setsToDownload = novelHeaders

        report("Number of sets to download for %s: %d".format(sgName, setsToDownload.length))
        setsToDownload foreach downloadSet(root, sgClient)
      }
    }
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
      IO.createFolder(newFolder)
      val photoSet = PhotoAlbumBuilder.buildPhotoSet(sgName, photoSetHeader, sgClient, report)
      report("\nDownloading set: %s".format(newFolder))
      photoSet.URLSaveLocationPairs foreach downloadFile(root, a)
    }

    def handleRename() {
      val allFiles = IO.listFolders(IO.concatPath(root, photoSetHeader.sgName))
      val temp = allFiles.filter(s => s.contains(photoSetHeader.title))
      assume(temp.length == 1, "Trying to perform a rename where it is not necessary: %s".format(photoSetHeader.relativeSaveLocation))
      val oldFolder = temp.head
      report("\nSet: %s; was previously in MR so the date differs, renaming it to: %s".format(oldFolder, newFolder))
      IO.rename(oldFolder, newFolder)
    }

    def requiresRename: Boolean = {
      val sgFolder = IO.concatPath(root, photoSetHeader.sgName)
      IO.exists(sgFolder) && {
        val allFiles = IO.listContent(IO.concatPath(root, photoSetHeader.sgName))
        !(allFiles.filter(s => s.contains(photoSetHeader.title)).isEmpty)
      }
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
