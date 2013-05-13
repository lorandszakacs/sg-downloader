package home.sg.util

import java.io.File

object TestDataResolver {

  val testDataFolder = "src/test/resources/scala/"
  def getTestDataFolderForClass(c: Class[_]): String = {

    val path = new File(new File(".").getAbsolutePath() + "/" + testDataFolder +
      c.getPackage().getName().replace(".", "/")).getCanonicalFile().getAbsolutePath() + "/"
    path
  }
}