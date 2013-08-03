package home.sg.client

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import home.sg.parser.html.PhotoSet
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DownloaderTest extends FunSuite {

  test("download Opaque") {
    val sgName = "Opaque"
    val rootFolder = "/Users/lorand/Downloads/temp/"
    val sgClient = new SGClient()
    sgClient.login(LoginInfo.user, LoginInfo.pwd)
    val downloader = new Downloader(sgName, sgClient)
    try { downloader.download(rootFolder) } finally { sgClient.cleanUp }
  }

}