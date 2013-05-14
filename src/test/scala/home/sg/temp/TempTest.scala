package home.sg.temp

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import java.io.File
import org.scalatest.junit.JUnitRunner
import home.sg.util.TestDataResolver
import home.sg.util.IO

@RunWith(classOf[JUnitRunner])
class TempTest extends FunSuite {

  test("renaming a file") {
    val originalName = "/Users/lorand/Downloads/zzzz/test"
    val newName = "/Users/lorand/Downloads/zzzz/test2"
    IO.rename(originalName, newName)
  }
}