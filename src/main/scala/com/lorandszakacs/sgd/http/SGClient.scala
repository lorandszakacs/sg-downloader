/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2014 Lorand Szakacs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.lorandszakacs.util.http

import java.io.IOException
import java.util.ArrayList

import scala.io.Source

import org.apache.http.HttpEntity
import org.apache.http.HttpStatus
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils

import com.lorandszakacs.util.io.IO

private object SiteInfo {
  val HomePageURL = "http://suicidegirls.com/"
  val LoginURL = "http://suicidegirls.com/login/"
  val LogoutURL = "http://suicidegirls.com/logout/"

  def sgAlbumPageURL(sgName: String) =
    String.format("%s/members/%s/albums/", HomePageURL, sgName)

  def createLoginPost(user: String, pwd: String) = {
    val loginPost = new HttpPost(SiteInfo.LoginURL);
    val entityBody = new ArrayList[NameValuePair]();
    entityBody.add(new BasicNameValuePair("action", "process_login"))
    entityBody.add(new BasicNameValuePair("username", user))
    entityBody.add(new BasicNameValuePair("password", pwd))
    loginPost.setEntity(new UrlEncodedFormEntity(entityBody));
    loginPost
  }

  def logoutGetRequest = new HttpGet(SiteInfo.LogoutURL)

}

class SGClient() {
  val httpClient = new DefaultHttpClient();

  def getPage(URL: String): List[String] = {
    val get = new HttpGet(URL)
    val response = httpClient.execute(get)
    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
      throw new RuntimeException("invalid page %s: ".format(URL))

    val entity = response.getEntity()
    assume(entity != null, "Entity should never be null")
    val content: Array[Byte] = IO.getByteArrayFromInputStream(entity.getContent())
    val source = Source.fromString(new String(content))
    source.getLines.toList
  }

  def getAlbumPage(sgName: String): List[String] = {
    val albumsURL = SiteInfo.sgAlbumPageURL(sgName)
    getPage(albumsURL)
  }

  def login(user: String, pwd: String) {
    val loginPost = SiteInfo.createLoginPost(user, pwd)
    val postResponse = httpClient.execute(loginPost);
    val postEntity = postResponse.getEntity();
    consume(postEntity);
    val javaCookies = httpClient.getCookieStore().getCookies()
    if (javaCookies.isEmpty) {
      throw new LoginUnknownException("did not receive any cookies from server")
    } else {
      val cookies = javaCookies.toString()
      if (cookies.contains("Incorrect+username+or+password"))
        throw new LoginInvalidUserOrPasswordExn("Incorrect username or password")
    }
  }

  def logout() {
    val response = httpClient.execute(SiteInfo.logoutGetRequest);
    consume(response.getEntity())
    httpClient.getCookieStore().clear()
  }

  def cleanUp() {
    try {
      logout()
    } catch { case t: Throwable => reportError("Logout failed") }
    shutdown()
  }

  /**
   * magic number, it's the size of that image you receive
   * whenever you access things for which you don't have
   * authorization
   */
  private val InvalidContentLength = 26365

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
   *   UnknownSGException - when somehow we've read an empty entity and when
   *   some other miscellaneous httClient exception occurs.
   *
   */
  def getSetImage(URL: String): Array[Byte] = {

    def getRequestEntity() = {
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

    val entity = getRequestEntity()
    val inputSize = entity.getContentLength().toInt

    inputSize match {
      case x if x < 0 => {
        consume(entity)
        throw new UnknownSGException("No content for: %s".format(URL))
      }
      case `InvalidContentLength` => {
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

  private val reportError: (Any => Unit) = { (x: Any) => System.err.println(x) }

  private def consume(ent: HttpEntity) = {
    EntityUtils.consume(ent);
  }

}