package home.sg.parser

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import home.sg.util.TestDataResolver
import java.io.File

@RunWith(classOf[JUnitRunner])
class SGSetAlbumTest extends FunSuite {

  private val nahpSetNames = List(
    "Girl Next Door", "Omnibot", "Sabor a Mi", "Sunday Vouyeurist", "Wii", "Chiaroscuro",
    "Radiant Morning", "Inspiration", "View Master", "Sun Flares", "Please, Don't Hang Up",
    "Bad Beat")

  private val sashSetNames = List(
    "The Grove", "Arboraceous", "Vowed", "On  Top", "Nirvana", "Room 224", "Haunted", "Gets Wet",
    "Buffalo", "Unwind", "Sunset Beach", "Octagon", "Alone Together", "REDRUM", "California Girls",
    "pancake breakfast", "Phone  Sex", "Hangover", "Trick or Treat", "Spread the Love", "Au Naturel",
    "Sugar Lips", "Sunkissed", "Nymph", "Dust Storm", "Slither", "Mattress", "Sushi", "Lovely Loser", "Trashy")

  private def getTestSourceFile(fileName: String) = {
    val filePath = TestDataResolver.getTestDataFolderForClass(classOf[SGSetAlbumPageParserTest]) + fileName
    scala.io.Source.fromFile(new File(filePath))
  }

  test("Nahp Set-Album Page, 12 total sets") {
    val nahpSource = getTestSourceFile(SGParserTestData.nahpSetAlbumPage)
    val result = new SGSetAlbum("Nahp", nahpSource)
    assert(result.sets.length === 12)
    assert(result.pinkSets.length === 12)
    assert(result.mrSets.length === 0)
    (assert(result.sets.map(_.setTitle) === nahpSetNames))
  }

  test("Sash Set-Album Page, 30 total sets") {
    val sashSource = getTestSourceFile(SGParserTestData.sashSetAlbumPage)
    val result = new SGSetAlbum("Sash", sashSource)
    assert(result.sets.length === 30)
    assert(result.pinkSets.length === 27)
    assert(result.mrSets.length === 3)
    (assert(result.sets.map(_.setTitle) === sashSetNames))
  }
}