package home.sg.client

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AssumptionsTest extends FunSuite {

  test("verify invalid image size") {
    val sgClient = new SGClient(false)

    //trying to get an image without logging in:
    val validImage = "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/01.jpg"
    try {
      val result = sgClient.getSetImage(validImage)
      fail("should have thrown exception")
    } catch {
      case rte: RuntimeException => assert(rte.getMessage().contains("invalid images"))
      case th: Throwable => fail("expected different outcome")
    }
  }
}