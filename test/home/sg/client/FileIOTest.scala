package home.sg.client

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class FileIOTest extends FunSuite {

  test("a") {
    val folder = "/Users/lorand/Downloads/temp/temp" //temp/subdir1/ass.txt
    FileIO.deleteFolder(folder);
  }

}