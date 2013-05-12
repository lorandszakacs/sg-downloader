package home.sg.client
import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import home.sg.util.IO
import org.apache.http.util.EntityUtils

@RunWith(classOf[JUnitRunner])
class MockTest extends FunSuite {

  test("writeTodile") {
    val client = new SGClient(true);
    client.login(LoginInfo.user, LoginInfo.pwd)
    val pageResponse = client.get("http://suicidegirls.com/girls/Epine/photos/+Dynamite++");
    val source = scala.io.Source.fromInputStream(pageResponse.getEntity().getContent())
    val content = source.getLines().toList

    //    IO.getByteArrayFromInputStream(pageResponse.getEntity().getContent(), contentLength)
    IO.writeToFile(content.mkString.getBytes(), "/Users/lorand/Downloads/temp/asString.html")
    EntityUtils.consume(pageResponse.getEntity())
    println("done")
    client.cleanUp
  }

}