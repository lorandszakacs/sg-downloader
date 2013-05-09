package home.sg.client

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import home.sg.parser.PhotoSetInfo

@RunWith(classOf[JUnitRunner])
class DownloaderTest extends FunSuite {

  test("download Aro") {
    val rootFolder = "/Users/lorand/Downloads/temp/"
    val levelOfReporting = new LevelOfReporting(2)
    val downloader = new Downloader("Aro", LoginInfo.user, LoginInfo.pwd, levelOfReporting)
    downloader.download(rootFolder)
  }

}