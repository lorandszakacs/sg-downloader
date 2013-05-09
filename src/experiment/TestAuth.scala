package experiment

import java.util.ArrayList
import org.apache.http.Consts
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.cookie.Cookie
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils;
import scala.io.Source

object TestAuth {

  def main(args: Array[String]): Unit = {
    val client = new SGClient()
    try {

      //      val httpget = new HttpGet("http://suicidegirls.com/login/");
      //      val getResponse = httpclient.execute(httpget);
      //      val getEntity = getResponse.getEntity();
      //      System.out.println("Login form get: " + getResponse.getStatusLine());
      //      EntityUtils.consume(getEntity);
      //      System.out.println("Initial set of cookies:");
      //      val cookiesGet = httpclient.getCookieStore().getCookies();
      //      if (cookiesGet.isEmpty()) {
      //        println("None");
      //      } else {
      //        List(cookiesGet).map(x => x.toString()).foreach(println)
      //      }
      client.login("Lorand", "xOG2rokX")

      println("--------------- trying to get an image ------------")
      val img = "http://img.suicidegirls.com/media/girls/Sash/photos/Arboraceous/01.jpg"
      client.get(img)
    } finally {
      // When HttpClient instance is no longer needed,
      // shut down the connection manager to ensure
      // immediate deallocation of all system resources
      client.shutdown()
    }
  }
}

class SGClient() {
  val httpclient = new DefaultHttpClient();

  def login(user: String, pwd: String) {
    val httpost = new HttpPost("http://suicidegirls.com/login/");
    val nvps = new ArrayList[NameValuePair]();
    nvps.add(new BasicNameValuePair("action", "process_login"))
    //nvps.add(new BasicNameValuePair("referer", "/"))
    nvps.add(new BasicNameValuePair("username", user))
    nvps.add(new BasicNameValuePair("password", pwd))
    //nvps.add(new BasicNameValuePair("loginbutton", ""))

    httpost.setEntity(new UrlEncodedFormEntity(nvps));

    val postResponse = httpclient.execute(httpost);
    val postEntity = postResponse.getEntity();

    System.out.println("Login form get: " + postResponse.getStatusLine());
    EntityUtils.consume(postEntity);

    System.out.println("Post logon cookies:");
    val cookiesPost = httpclient.getCookieStore().getCookies();
    if (cookiesPost.isEmpty()) {
      println("None");
    } else {
      List(cookiesPost).map(x => x.toString()).foreach(println)
    }

  }

  /**
   * magic number, it's the size of that image you receive
   * when the server requests that you log in.
   */
  private val invalidContentLength = 26365

  def get(URL: String) = {
    val get = new HttpGet(URL)
    val response = httpclient.execute(get)
    val entity = response.getEntity()

    entity match {
      case null => {
        EntityUtils.consume(entity)
        None
      }
      case e => {
        val inputSize = entity.getContentLength().toInt
        if (entity.getContentLength() == invalidContentLength) {
          EntityUtils.consume(entity);
          None
        } else {
          val source = Source.fromInputStream(entity.getContent())
          val buff = new Array[Char](inputSize)
          source.copyToArray(buff, 0, inputSize)
          println("the size of what we got is: " + entity.getContentLength())
          source.close()
          EntityUtils.consume(entity)
          Some(buff)
        }
      }
    }
  }

  def shutdown() {
    httpclient.getConnectionManager().shutdown();
  }

  private val sgLoginURL = "http://suicidegirls.com/login/"

  private def consume(ent: HttpEntity) = {
    EntityUtils.consume(ent);
  }

}