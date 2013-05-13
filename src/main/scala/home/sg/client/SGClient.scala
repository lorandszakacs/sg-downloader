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
import scala.collection.mutable.Buffer
import scala.collection.mutable.ListBuffer
import java.io.IOException

private object SiteInfo {
  val homePageURL = "http://suicidegirls.com/"
  val loginURL = "http://suicidegirls.com/login/"
  val logoutURL = "http://suicidegirls.com/logout/"

  def createAlbumsURL(sgName: String) =
    String.format("%s/girls/%s/albums/", homePageURL, sgName)

  def createLoginPost(user: String, pwd: String) = {
    val loginPost = new HttpPost(SiteInfo.loginURL);
    val entityBody = new ArrayList[NameValuePair]();
    entityBody.add(new BasicNameValuePair("action", "process_login"))
    entityBody.add(new BasicNameValuePair("username", user))
    entityBody.add(new BasicNameValuePair("password", pwd))
    loginPost.setEntity(new UrlEncodedFormEntity(entityBody));
    loginPost
  }

  def createLogoutGet() = new HttpGet(SiteInfo.logoutURL)

}

class SGClient(silent: Boolean) {
  val httpClient = new DefaultHttpClient();

  /**
   * //TODO
   * @param URL
   * @return
   */
  def get(URL: String): List[String] = {
    val get = new HttpGet(URL)
    val response = httpClient.execute(get)
    val entity = response.getEntity()
    assume(entity != null, "Entity should never be null")
    val content = Source.fromInputStream(entity.getContent())
    val separator = "12345678"
    val result = content.getLines.toList.mkString(separator)
    consume(entity)
    result.split(separator).toList
  }

  def getSetAlbumPageSource(sgName: String): List[String] = {
    val albumsURL = SiteInfo.createAlbumsURL(sgName)
    println(albumsURL)
    get(albumsURL)
  }

  def login(user: String, pwd: String) {
    def failedToLogOn(reason: String) = throw new HttpClientException("login failed: %s".format(reason))

    val loginPost = SiteInfo.createLoginPost(user, pwd)
    val postResponse = httpClient.execute(loginPost);
    val postEntity = postResponse.getEntity();
    consume(postEntity);
    val javaCookies = httpClient.getCookieStore().getCookies()
    if (javaCookies.isEmpty) {
      throw new LoginUnknownException("did not receive any cookies from server")
    } else {
      val cookies = javaCookies.toString()
      report("Cookies after log on: " + cookies)
      if (cookies.contains("Incorrect+username+or+password"))
        throw new LoginInvalidUserOrPasswordExn("Incorrect username or password")
    }
  }

  def logout() {
    val response = httpClient.execute(SiteInfo.createLogoutGet());
    consume(response.getEntity())
    httpClient.getCookieStore().clear()
  }

  def cleanUp() {
    logout()
    shutdown()
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
   *
   * Exceptions:
   *   HttpClientException - when the httpclient failed to create a proper GET request
   *   FileDownloadException - when it failed to read and save the image file
   *   LoginLostException - when we lost login privileges
   *
   *   InexistentFileException - we've reached the last valid file, it's safe to stop
   *   trying the other ones out.
   *
   *   UnknownSGException - when somehow we've read an empty entity and when
   *   some other miscellaneous httClient exception occurs.
   *
   */
  def getSetImage(URL: String): Array[Byte] = {

    def getEntity() = {
      try {
        val get = new HttpGet(URL)
        val response = httpClient.execute(get)
        val entity = response.getEntity()
        assume(entity != null, "Entity should never be null")
        entity
      } catch {
        case exn: Exception => throw new HttpClientException("Cannot create http GET:: %s".format(exn.getMessage()))
        case thw: Throwable => throw new UnknownSGException(thw.getMessage());
      }
    }

    val entity = getEntity()
    val inputSize = entity.getContentLength().toInt
    report("SGClient->get, just read input of size: " + inputSize)

    inputSize match {
      case x if x < 0 => {
        consume(entity)
        throw new UnknownSGException("No content for: %s".format(URL))
      }
      case `invalidContentLength` => {
        consume(entity)
        throw new LoginConnectionLostException("Starting to receive invalid images, login info lost @: %s".format(URL))
      }
      case _ => {
        try {
          val inputStream = entity.getContent()
          val buff = IO.getByteArrayFromInputStream(inputStream, inputSize)
          consume(entity)
          buff
        } catch {
          case twb: IOException => {
            System.err.println(twb.getMessage())
            throw new FileDownloadException(twb.getMessage())
          }
        }
      }
    }

  }

  private def shutdown() {
    httpClient.getConnectionManager().shutdown();
  }

  private val report: (Any => Unit) = if (silent) ((x: Any) => Unit) else ((x: Any) => println(x))
  private val reportError: (Any => Unit) = if (silent) { (x: Any) => Unit } else { (x: Any) => System.err.println(x) }

  private def consume(ent: HttpEntity) = {
    EntityUtils.consume(ent);
  }

}