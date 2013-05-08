package home.sg.util

import java.io.File

object TestDataResolver {

  val testDataFolder = "testdata/"
  def getTestDataFolderForClass(c: Class[_]): String = {
    new File(new File(".").getAbsolutePath() + "/" + testDataFolder +
      c.getPackage().getName().replace(".", "/")).getCanonicalFile().getAbsolutePath() + "/"

  }
}