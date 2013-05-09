import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet

object testAuth {

  private val user = "Lorand"
  private val pwd = "jw0c1RMR"

  def main(args: Array[String]) = {
    val httpclient = new DefaultHttpClient();

    httpclient.getCredentialsProvider().setCredentials(
      new AuthScope("suicidegirls.com", 443),
      new UsernamePasswordCredentials(user, pwd))

    val httpget = new HttpGet("https://secure.suicidegirls.com/login")

    println("executing request" + httpget.getRequestLine())
    val response = httpclient.execute(httpget)
    val entity = response.getEntity();

    println("----------------------------------------");
    println(response.getStatusLine());
    response.getAllHeaders.map(println)
    if (entity != null) {
      System.out.println("Response content length: " + entity.getContentLength());
    }
    if (entity != null) {
      entity.consumeContent();
    }
    println("----------------")

    val img = new HttpGet("http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/01.jpg")
    println("executing request" + img.getRequestLine())
    val response2 = httpclient.execute(img)
    if (response2.getEntity().getContentLength() == 26365)
      println("WUHUUUUUUU")
    else println("SHIT PISHH CUNT FUCK")
    // When HttpClient instance is no longer needed, 
    // shut down the connection manager to ensure
    // immediate deallocation of all system resources
    httpclient.getConnectionManager().shutdown();

  }

}