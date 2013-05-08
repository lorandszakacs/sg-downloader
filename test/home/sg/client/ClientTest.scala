package home.sg.client

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import home.sg.parser.SetAlbum

@RunWith(classOf[JUnitRunner])
class ClientTest extends FunSuite {

  private val user = "Lorand"
  private val pwd = "Hs79J8ts"

  private val nahpSetNames = List(
    "Girl Next Door", "Omnibot", "Sabor a Mi", "Sunday Vouyeurist", "Wii", "Chiaroscuro",
    "Radiant Morning", "Inspiration", "View Master", "Sun Flares", "Please, Don't Hang Up",
    "Bad Beat")

  test("get album page source") {
    val sgClient = new Client(user, pwd)
    val invalidURI = "http://img.suicidegirls.com/media/albums/0/36/33360/150601.jpg"
    val source = sgClient.getSetAlbumPageSource("Nahp");
    val result = new SetAlbum("Nahp", source);

    assert(result.sets.length === 12)
    assert(result.pinkSets.length === 12)
    assert(result.mrSets.length === 0)
    (assert(result.sets.map(_.setTitle) === nahpSetNames))

  }
}