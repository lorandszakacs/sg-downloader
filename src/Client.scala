

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
import org.apache.http.HttpStatus
import scala.Array.canBuildFrom

object Client {

  val user = "Lorand"
  val password = "jw0c1RMR"

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
      info.add(new BasicNameValuePair(userNameField, user))
      info.add(new BasicNameValuePair(pwdField, password))
      info.add(new BasicNameValuePair(loginButtonField, ""))
      info
    }
    val post = new HttpPost(loginURL)
    post.setEntity(new UrlEncodedFormEntity(loginForm))
    post
  }

  //Cookie:j_s=SG; l=i; km_ai=H40FYjpnChALEj%2B5xPxLeNfDQ3A%3D; PHPSESSID=bfbbb8a7164eef786b7473c05585c9b5; SGKEY=mpEvs9lsB6K8Q72bFX97CQcljrythUHL; __utma=207119761.777429434.1367984217.1367984217.1367984217.1; __utmb=207119761.2.10.1367984217; __utmc=207119761; __utmz=207119761.1367984217.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); kvcd=1367984239686; km_ni=Lorand; km_vs=1; km_lv=1367984240; km_uq=

  object cookieInfo {
    val acceptField = "Accept"
    val acceptCharsetField = "Accept-Charset"
    val acceptEncodingField = "Accept-Encoding"
    val acceptLangField = "Accept-Language"
    val connectionField = "Connection"
    val cookieField = "Cookie"
    val hostField = "Host"

    println(loginResponseInfo)
    val SGKEY = loginResponseInfo.filter(p => p._2.contains("SGKEY")).head._2.split(";", 10).head.substring("SGKEY=".length)
    val PHPSESSID = loginResponseInfo.filter(p => p._2.contains("PHPSESSID")).head._2.split(";", 10).head.substring("PHPSESSID=".length)
    val cookieFieldContent = "j_s=SG; PHPSESSID=%s; SGKEY=%s".format(PHPSESSID, SGKEY)

    val getRequestForm = {
      println("BUILDING FUCKING REQUEST FORM")
      val info = List[Header](
        new BasicHeader(cookieInfo.acceptField, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"),
        new BasicHeader(cookieInfo.acceptCharsetField, "ISO-8859-1,utf-8;q=0.7,*;q=0.3"),
        new BasicHeader(cookieInfo.acceptEncodingField, "gzip,deflate,sdch"),
        new BasicHeader(cookieInfo.acceptLangField, "en-US,en;q=0.8"),
        new BasicHeader(cookieInfo.connectionField, "keep-alive"),
        new BasicHeader(cookieInfo.cookieField, cookieFieldContent),
        new BasicHeader(cookieInfo.hostField, "img.suicidegirls.com"))
      info map println
      info.toArray
    }
  }

  def httpClient = {
    val c = new DefaultHttpClient()
    c.setCookieStore(new BasicCookieStore())
    c.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
    c
  }

  def loginResponseInfo = {
    val response = httpClient.execute(loginPostMethod)
    val responseHeaders = response.getAllHeaders().map(h => (h.getName(), h.getValue()))
    response.getEntity().getContent().close();
    responseHeaders
  }

  private def getImageRequest(imageURI: String) = {
    val httpGet: HttpGet = new HttpGet(imageURI)
    println(cookieInfo.acceptCharsetField)
    httpGet.setHeaders(cookieInfo.getRequestForm);
    httpGet
  }

  def get(imageURI: String) = {
    httpClient.execute(getImageRequest(imageURI)).getEntity()
  }

  def shutdown() {
    httpClient.getConnectionManager().shutdown()
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

  val invalidContentLength = 26365

  //
  //  //when you try to access an image without a cookie you always get back that
  //  //thumbnail image back
  //  val invalidContentLength = 26365
  //
  //  def get(imageURI: String) = {
  //    val httpGet: HttpGet = new HttpGet(imageURI)
  //    httpGet.setHeaders(requestHeaders);
  //    requestHeaders map println
  //    val response = httpClient.execute(httpGet)
  //    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND)
  //      None
  //    else Some(response.getEntity())
  //  }
  //
  //  def getSetAlbumPageSource(sgName: String) = {
  //    val sgURL = "http://suicidegirls.com/"
  //    val albumsURL = String.format("%s/girls/%s/albums/", sgURL, sgName)
  //    val httpGet: HttpGet = new HttpGet(albumsURL)
  //    val response = httpClient.execute(httpGet)
  //    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND)
  //      throw new RuntimeException("Could not retrieve album page for: %;; at URL: %s".format(sgName, albumsURL))
  //    else
  //      scala.io.Source.fromInputStream(response.getEntity().getContent())
  //  }
  //
  //  private val loginPostMethod: HttpPost = {
  //    val loginURL = "http://suicidegirls.com/login/"
  //    val actionField = "action"
  //    val refererField = "referer"
  //    val userNameField = "username"
  //    val pwdField = "password"
  //    val loginButtonField = "loginbutton"
  //    val loginForm = {
  //      val info = new ArrayList[BasicNameValuePair]()
  //      info.add(new BasicNameValuePair(actionField, "process_login"))
  //      info.add(new BasicNameValuePair(refererField, "/"))
  //      info.add(new BasicNameValuePair(userNameField, user))
  //      info.add(new BasicNameValuePair(pwdField, password))
  //      info.add(new BasicNameValuePair(loginButtonField, ""))
  //      info
  //    }
  //    val post = new HttpPost(loginURL)
  //    post.setEntity(new UrlEncodedFormEntity(loginForm))
  //    post
  //  }
  //
  //  private lazy val requestHeaders = {
  //    def parseSGCookie(responses: List[(String, String)]) =
  //      responses.filter(p => p._2.contains("SGKEY")).head._2.split(";", 10).head.substring("SGKEY=".length)
  //
  //    def parsePHPSESSID(responses: List[(String, String)]) =
  //      responses.filter(p => p._2.contains("PHPSESSID")).head._2.split(";", 10).head.substring("PHPSESSID=".length)
  //
  //    def checkForLoginError(responses: List[(String, String)], r: HttpResponse) {
  //      val errors = responses.filter(p => p._2.contains("LOGIN_ERROR") && !p._2.contains("LOGIN_ERROR=deleted"))
  //      if (errors.length != 0) {
  //        r.getEntity().getContent().close();
  //        throw new RuntimeException("LOGIN FAILED\n:" + errors.mkString("\n"))
  //      }
  //    }
  //
  //    println("Trying to login.")
  //    val response = httpClient.execute(loginPostMethod)
  //    val responseHeaders = response.getAllHeaders().map(h => (h.getName(), h.getValue())).toList
  //    println("=======")
  //    responseHeaders map println
  //    println("=======")
  //    checkForLoginError(responseHeaders, response)
  //
  //    val SGKEY = parseSGCookie(responseHeaders);
  //    val PHPSESSID = parsePHPSESSID(responseHeaders);
  //    val cookieFieldContent = "j_s=SG; PHPSESSID=%s; SGKEY=%s".format(PHPSESSID, SGKEY)
  //
  //    def getRequestHeaders = {
  //      val acceptField = "Accept"
  //      val acceptCharsetField = "Accept-Charset"
  //      val acceptEncodingField = "Accept-Encoding"
  //      val acceptLangField = "Accept-Language"
  //      val connectionField = "Connection"
  //      val cookieField = "Cookie"
  //      val hostField = "Host"
  //      val userAgent = "User-Agent"
  //      val info = List[Header](
  //        new BasicHeader(acceptField, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"),
  //        new BasicHeader(acceptCharsetField, "ISO-8859-1,utf-8;q=0.7,*;q=0.3"),
  //        new BasicHeader(acceptEncodingField, "gzip,deflate,sdch"),
  //        new BasicHeader(acceptLangField, "en-US,en;q=0.8"),
  //        new BasicHeader(connectionField, "keep-alive"),
  //        new BasicHeader(cookieField, cookieFieldContent),
  //        new BasicHeader(hostField, "img.suicidegirls.com"),
  //        new BasicHeader(userAgent, "(Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_3) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31)"))
  //      info.toArray
  //    }
  //    val entity = response.getEntity();
  //    assume(entity != null)
  //    entity.consumeContent();
  //    entity.getContent().close();
  //    getRequestHeaders
  //  }
  //
  //  def shutdown() {
  //    httpClient.getConnectionManager().shutdown()
  //  }
  //
  //  private val httpClient = {
  //    val c = new DefaultHttpClient()
  //    c.setCookieStore(new BasicCookieStore())
  //    c.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
  //    c
  //  }

}