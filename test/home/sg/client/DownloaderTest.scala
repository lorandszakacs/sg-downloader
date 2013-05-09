package home.sg.client

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import home.sg.parser.PhotoSetInfo

@RunWith(classOf[JUnitRunner])
class DownloaderTest extends FunSuite {

  test("download Nahp") {
    def onlyOneSet(set: PhotoSetInfo) =
      set.setTitle.contains("Next")
      
    val rootFolder = "/Users/lorand/Downloads/temp/"
    val downloader = new Downloader("Nahp", LoginInfo.user, LoginInfo.pwd, false)
    downloader.download(rootFolder, onlyOneSet)
    //    val downloader = new Downloader("Aro", LoginInfo.user, LoginInfo.pwd, false)
    //    downloader.download(rootFolder)
  }

}