package experiment

import java.util.ArrayList;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

object TestAuth {

  def main(args: Array[String]): Unit = {

    val httpclient = new DefaultHttpClient();
    try {
      val httpget = new HttpGet("http://suicidegirls.com/login/");

      val getResponse = httpclient.execute(httpget);
      val getEntity = getResponse.getEntity();

      System.out.println("Login form get: " + getResponse.getStatusLine());
      EntityUtils.consume(getEntity);

      System.out.println("Initial set of cookies:");
      val cookiesGet = httpclient.getCookieStore().getCookies();
      if (cookiesGet.isEmpty()) {
        println("None");
      } else {
        List(cookiesGet).map(x => x.toString()).foreach(println)
      }

      val httpost = new HttpPost("http://suicidegirls.com/login/");

      val nvps = new ArrayList[NameValuePair]();
      nvps.add(new BasicNameValuePair("action", "process_login"))
      nvps.add(new BasicNameValuePair("referer", "/"))
      nvps.add(new BasicNameValuePair("username", "Lorand"))
      nvps.add(new BasicNameValuePair("password", "4K4HaHtv"))
      nvps.add(new BasicNameValuePair("loginbutton", ""))

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

      println("--------------- trying to get an image ------------")
      val img = "http://img.suicidegirls.com/media/girls/Sash/photos/Arboraceous/01.jpg"
      val imgGet = new HttpGet(img)
      val imgResponse = httpclient.execute(imgGet)
      val imgEntity = imgResponse.getEntity()
      println("the size of what we got is: " + imgEntity.getContentLength())
      EntityUtils.consume(imgEntity);

    } finally {
      // When HttpClient instance is no longer needed,
      // shut down the connection manager to ensure
      // immediate deallocation of all system resources
      httpclient.getConnectionManager().shutdown();
    }
  }

}