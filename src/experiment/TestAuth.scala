package experiment

import home.sg.client.SGClient

object TestAuth {

  def main(args: Array[String]): Unit = {
    val client = new SGClient(false)
    try {

      client.login("Lorand", "xOG2rokX")

      val img = "http://img.suicidegirls.com/media/girls/Sash/photos/Arboraceous/01.jpg"
      val imgArray = client.get(img)
      imgArray match {
        case Some(buff) => println("We read an image of the size: " + buff.length)
        case None => println("Fetching image failed")
      }

    } finally {
      client.shutdown()
    }
  }
}