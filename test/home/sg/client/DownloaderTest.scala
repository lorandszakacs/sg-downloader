package home.sg.client

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import home.sg.parser.PhotoSet

@RunWith(classOf[JUnitRunner])
class DownloaderTest extends FunSuite {

  //  test("download Sash") {
  //    downloadSG("Sash", new LevelOfReporting(1))
  //  }

    test("download Epine") {
      downloadSG("Epine", new LevelOfReporting(3))
    }

  //  test("download Radeo") {
  //    downloadSG("Radeo", { x => x.setTitle.contains("Bettie") }, new LevelOfReporting(2))
  //  }

  //    test("download Rigel") {
  //      downloadSG("Rigel",  new LevelOfReporting(1))
  //    }

  //  test("download Nya") {
  //    downloadSG("Nya")
  //  }
  //  
  //  test("download Lyxzen") {
  //    downloadSG("Lyxzen")
  //  }

  private def downloadSG(sgName: String, lor: LevelOfReporting) {
    val rootFolder = "/Users/lorand/Downloads/temp/"
    val downloader = new Downloader(sgName, LoginInfo.user, LoginInfo.pwd, lor)
    downloader.download(rootFolder)
  }

  private def downloadSG(sgName: String, filter: (PhotoSet) => Boolean, lor: LevelOfReporting) {
    val rootFolder = "/Users/lorand/Downloads/temp/"
    val downloader = new Downloader(sgName, LoginInfo.user, LoginInfo.pwd, lor)
    downloader.download(rootFolder, filter)
  }

  private def downloadSG(sgName: String) {
    downloadSG(sgName, new LevelOfReporting(0))
  }

}