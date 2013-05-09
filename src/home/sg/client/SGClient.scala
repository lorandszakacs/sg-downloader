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

class SGClient(silent: Boolean) {
  val httpclient = new DefaultHttpClient();

  def getSetAlbumPageSource(sgName: String) = {
    //    val albumsURL = String.format("http://suicidegirls.com/girls/%s/albums/", sgName)
    //    val u = get(albumsURL)
    //    u match {
    //      case Some(buff) => Source.fromChars(buff)
    //      case None => throw new RuntimeException("unable to read the albums for: %".format(sgName))
    //    }
    val sgURL = "http://suicidegirls.com/"
    val albumsURL = String.format("%s/girls/%s/albums/", sgURL, sgName)
    val u = new java.net.URL(albumsURL)
    val in = scala.io.Source.fromURL(u)
    in
  }

  def login(user: String, pwd: String) {
    def createPost() = {
      val httpost = new HttpPost("http://suicidegirls.com/login/");
      val nvps = new ArrayList[NameValuePair]();
      nvps.add(new BasicNameValuePair("action", "process_login"))
      //nvps.add(new BasicNameValuePair("referer", "/"))
      nvps.add(new BasicNameValuePair("username", user))
      nvps.add(new BasicNameValuePair("password", pwd))
      //nvps.add(new BasicNameValuePair("loginbutton", ""))
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
   * when the server requests that you log in.
   */
  private val invalidContentLength = 26365

  val fileSkipMessage = "skipping".toCharArray()
  /**
   * @param URL the URL of the resource you want to fetch
   * @return Option[Array[Char]] containing the contents
   * of the thing received
   */
  def get(URL: String): Option[Array[Char]] = {
    val get = new HttpGet(URL)
    val response = httpclient.execute(get)
    val entity = response.getEntity()

    entity match {
      case null => {
        consume(entity)
        None
      }
      case _ => {
        val inputSize = entity.getContentLength().toInt
        report("SGClient->get, just read input of size: " + inputSize)

        inputSize match {
          case x if x < 0 => {
            consume(entity)
            throw new RuntimeException("No content for: %s".format(URL))
          }

          case `invalidContentLength` => {
            consume(entity)
            throw new RuntimeException("Lost login connection @: %s".format(URL))
          }

          case x if x < invalidContentLength => {
            //this means that the link has not been found, so just ignore
            //it is normal since we construct images ranging from 01 to 90
            //because we cannot find out what the size of the set is.
            consume(entity)
            Some(fileSkipMessage)
          }

          case _ => {
            val source = Source.fromInputStream(entity.getContent())
            val buff = new Array[Char](inputSize)
            source.copyToArray(buff, 0, inputSize)
            source.close()
            consume(entity)
            Some(buff)
          }
        }
        //        if (inputSize > 0 && inputSize <= invalidContentLength) {
        //          consume(entity);
        //          Some(fileSkipMessage)
        //        } else if (inputSize < 0) {
        //          consume(entity)
        //          throw new RuntimeException("No content for: %s".format(URL))
        //        } else {
        //          val source = Source.fromInputStream(entity.getContent())
        //          val buff = new Array[Char](inputSize)
        //          source.copyToArray(buff, 0, inputSize)
        //          source.close()
        //          consume(entity)
        //          Some(buff)
        //        }
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