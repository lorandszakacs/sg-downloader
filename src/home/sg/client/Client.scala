package home.sg.client

import java.util.ArrayList
import org.apache.http.message.BasicNameValuePair
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.client.params.ClientPNames
import org.apache.http.client.params.CookiePolicy
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.Header
import org.apache.http.message.BasicHeader
import org.apache.http.client.methods.HttpGet
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus

class Client(val user: String, val password: String) {

  //when you try to access an image without a cookie you always get back that
  //thumbnail image back
  val invalidContentLength = 26365

  def get(imageURI: String) = {
    val httpGet: HttpGet = new HttpGet(imageURI)
    httpGet.setHeaders(requestHeaders);
    val response = httpClient.execute(httpGet)
    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND)
      None
    else Some(response.getEntity())
  }

  def getSetAlbumPageSource(sgName: String) = {
    val sgURL = "http://suicidegirls.com/"
    val albumsURL = String.format("%s/girls/%s/albums/", sgURL, sgName)
    val httpGet: HttpGet = new HttpGet(albumsURL)
    val response = httpClient.execute(httpGet)
    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND)
      throw new RuntimeException("Could not retrieve album page for: %;; at URL: %s".format(sgName, albumsURL))
    else
      scala.io.Source.fromInputStream(response.getEntity().getContent())
  }

  private def loginPostMethod: HttpPost = {
    val loginURL = "http://suicidegirls.com/login/"
    val actionField = "action"
    val refererField = "referer"
    val userNameField = "username"
    val pwdField = "password"
    val loginButtonField = "loginbutton"
    val loginForm = {
      val info = new ArrayList[BasicNameValuePair]()
      info.add(new BasicNameValuePair(actionField, "process_login"))
      info.add(new BasicNameValuePair(refererField, "/"))
      info.add(new BasicNameValuePair(userNameField, "Lorand"))
      info.add(new BasicNameValuePair(pwdField, "Hs79J8ts"))
      info.add(new BasicNameValuePair(loginButtonField, ""))
      info
    }
    val post = new HttpPost(loginURL)
    post.setEntity(new UrlEncodedFormEntity(loginForm))
    post
  }

  private def requestHeaders = {
    def parseSGCookie(responses: List[(String, String)]) =
      responses.filter(p => p._2.contains("SGKEY")).head._2.split(";", 10).head.substring("SGKEY=".length)

    def parsePHPSESSID(responses: List[(String, String)]) =
      responses.filter(p => p._2.contains("PHPSESSID")).head._2.split(";", 10).head.substring("PHPSESSID=".length)

    def checkForLoginError(responses: List[(String, String)], r: HttpResponse) {
      val errors = responses.filter(p => p._2.contains("LOGIN_ERROR") && !p._2.contains("LOGIN_ERROR=deleted"))
      if (errors.length != 0) {
        r.getEntity().getContent().close();
        throw new RuntimeException("LOGIN FAILED\n:" + errors.mkString("\n"))
      }
    }

    println("Trying to login.")
    val response = httpClient.execute(loginPostMethod)
    val responseHeaders = response.getAllHeaders().map(h => (h.getName(), h.getValue())).toList
    checkForLoginError(responseHeaders, response)

    val SGKEY = parseSGCookie(responseHeaders);
    val PHPSESSID = parsePHPSESSID(responseHeaders);
    val cookieFieldContent = "j_s=SG; PHPSESSID=%s; SGKEY=%s".format(PHPSESSID, SGKEY)

    val getRequestHeaders = {
      val acceptField = "Accept"
      val acceptCharsetField = "Accept-Charset"
      val acceptEncodingField = "Accept-Encoding"
      val acceptLangField = "Accept-Language"
      val connectionField = "Connection"
      val cookieField = "Cookie"
      val hostField = "Host"
      val info = List[Header](
        new BasicHeader(acceptField, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"),
        new BasicHeader(acceptCharsetField, "ISO-8859-1,utf-8;q=0.7,*;q=0.3"),
        new BasicHeader(acceptEncodingField, "gzip,deflate,sdch"),
        new BasicHeader(acceptLangField, "en-US,en;q=0.8"),
        new BasicHeader(connectionField, "keep-alive"),
        new BasicHeader(cookieField, cookieFieldContent),
        new BasicHeader(hostField, "img.suicidegirls.com"))

      println("Login succesfull")
      info.toArray
    }
    val entity = response.getEntity();
    assume(entity != null)
    entity.consumeContent();
    entity.getContent().close();
    getRequestHeaders
  }

  private def httpClient = {
    val c = new DefaultHttpClient()
    c.setCookieStore(new BasicCookieStore())
    c.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
    c
  }
}