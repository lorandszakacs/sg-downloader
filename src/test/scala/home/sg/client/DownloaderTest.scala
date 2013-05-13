package home.sg.client

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import home.sg.parser.PhotoSet
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DownloaderTest extends FunSuite {

  test("download Paula") {
    val sgName = "Paula"
    val lor = new LevelOfReporting(1)
    val rootFolder = "/Users/lorand/Downloads/temp/"
    val sgClient = new SGClient(true)
    sgClient.login(LoginInfo.user, LoginInfo.pwd)
    val downloader = new Downloader(sgName, sgClient, lor)
    try { downloader.download(rootFolder) } finally { sgClient.cleanUp }
  }

}