package home.sg.client

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import home.sg.parser.SetAlbum

@RunWith(classOf[JUnitRunner])
class DownloaderTest extends FunSuite {

  private val user = "Lorand"
  private val pwd = "MG7NvUiY"

  private val nahpSetNames = List(
    "Girl Next Door", "Omnibot", "Sabor a Mi", "Sunday Vouyeurist", "Wii", "Chiaroscuro",
    "Radiant Morning", "Inspiration", "View Master", "Sun Flares", "Please, Don't Hang Up",
    "Bad Beat")

  test("download") {
    val root = "/Users/lorand/Downloads/temp/sg"
    val sg = "Nahp"
    val silent = false
    val downloader = new Downloader(sg, user, pwd, silent)
    downloader.download(root)

  }
}