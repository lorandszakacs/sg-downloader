package home.sg.client

import org.apache.http.impl.client.DefaultHttpClient
import java.util.ArrayList
import org.apache.http.HttpEntity
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import scala.io.Source
import home.sg.util.IO

object SiteInfo {
  val sgURL = "http://suicidegirls.com/"

  def getAlbumsURL(sgName: String) = {
    String.format("%s/girls/%s/albums/", sgURL, sgName)
  }
}

class SGClient(silent: Boolean) {
  val httpclient = new DefaultHttpClient();

  def getSetAlbumPageSource(sgName: String) = {
    val albumsURL = SiteInfo.getAlbumsURL(sgName)
    val u = new java.net.URL(albumsURL)
    val in = scala.io.Source.fromURL(u)
    in
  }

  def login(user: String, pwd: String) {
    def createPost() = {
      val httpost = new HttpPost("http://suicidegirls.com/login/");
      val nvps = new ArrayList[NameValuePair]();
      nvps.add(new BasicNameValuePair("action", "process_login"))
      nvps.add(new BasicNameValuePair("username", user))
      nvps.add(new BasicNameValuePair("password", pwd))
      httpost.setEntity(new UrlEncodedFormEntity(nvps));
      httpost
    }
    def failedToLogOn(reason: String) = throw new RuntimeException("login failed: %s".format(reason))

    val httpost = createPost()
    val postResponse = httpclient.execute(httpost);
    val postEntity = postResponse.getEntity();
    consume(postEntity);

    val javaCookies = httpclient.getCookieStore().getCookies()
    if (javaCookies.isEmpty) {
      failedToLogOn("did not receive any cookies from other end")
    } else {
      val cookies = javaCookies.toString()
      report("Cookies after log on: " + cookies)
      if (cookies.contains("Incorrect+username+or+password"))
        failedToLogOn(cookies)
    }
  }

  /**
   * magic number, it's the size of that image you receive
   * whenever you access things for which you don't have
   * authorization
   */
  private val invalidContentLength = 26365

  /**
   * @param URL the URL of the set Image we want to fetch
   * @return Option[Array[Char]] containing the image
   * or None if we provided an image link that was
   * invalid i.e.
   * "http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/90.jpg"
   */
  def getSetImage(URL: String): Option[Array[Byte]] = {
    val get = new HttpGet(URL)
    val response = httpclient.execute(get)
    val entity = response.getEntity()

    assume(entity != null, "Entity should never be null")
    val inputSize = entity.getContentLength().toInt
    report("SGClient->get, just read input of size: " + inputSize)

    inputSize match {
      case x if x < 0 => {
        consume(entity)
        throw new RuntimeException("No content for: %s".format(URL))
      }

      case `invalidContentLength` => {
        consume(entity)
        throw new RuntimeException("Starting to receive invalid images, login info lost @: %s".format(URL))
      }

      case x if x < invalidContentLength => {
        //this means that the link has not been found, so just ignore
        //it is normal since we construct images ranging from 01 to 90
        //because we cannot find out what the size of the set is.
        consume(entity)
        None
      }

      case _ => {
        val inputStream = entity.getContent()
        val buff = IO.getByteArrayFromInputStream(inputStream, inputSize)
        consume(entity)
        Some(buff)
      }
    }
  }

  def shutdown() {
    httpclient.getConnectionManager().shutdown();
  }

  private def report = if (silent) ((x: Any) => Unit) else ((x: Any) => println(x))

  private def consume(ent: HttpEntity) = {
    EntityUtils.consume(ent);
  }

}