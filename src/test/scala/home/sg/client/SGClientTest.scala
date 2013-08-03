package home.sg.client
import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SGClientTest extends FunSuite {

  test("login and fetch a few images") {
    val sgClient = new SGClient()
    try {
      sgClient.login(LoginInfo.user, LoginInfo.pwd)

      val listOfImages = List(
        ("http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/01.jpg", 252949),
        ("http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/02.jpg", 238218),
        ("http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/03.jpg", 224634),
        ("http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/04.jpg", 218022))

      listOfImages foreach { p =>
        val buff = sgClient.getSetImage(p._1)
        assert(buff.length === p._2, "size of image received did not match what was expected")
      }
    } finally {
      sgClient.cleanUp()
    }
  }

  test("logout") {
    val sgClient = new SGClient()
    try {
      sgClient.login(LoginInfo.user, LoginInfo.pwd)

      val validImage = ("http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/06.jpg", 188932)

      val buff = sgClient.getSetImage(validImage._1)
      assert(buff.length === validImage._2, "receiving images while logged in, failed")

      sgClient.logout()

      try {
        sgClient.getSetImage(validImage._1)
        fail("should have thrown an exception")
      } catch {
        case rte: LoginConnectionLostException => assert(true)
        case t: Throwable => fail("unexpected exception was thrown")
      }
    } finally {
      sgClient.cleanUp()
    }
  }

}