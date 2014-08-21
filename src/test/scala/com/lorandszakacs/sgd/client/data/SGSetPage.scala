package com.lorandszakacs.sgd.client.data

import scala.io.Source
import com.lorandszakacs.commons.html.Html
import spray.http.Uri

object SGSetPage {
  def html = {
    val resourceName = s"${getClass.getSimpleName().replace("$", "")}.html"
    val URL = getClass.getResource(resourceName)
    val source = Source.fromURL(URL)
    Html(source.getLines().mkString("\n"))
  }

  val photoSetURIs = List(
    "/girls/dwam/album/1239337/adieu-tristesse/",
    "/girls/dwam/album/977051/limportance-d-etre-ernest/",
    "/girls/dwam/album/976671/midsummer-crown/",
    "/girls/dwam/album/976285/woad/",
    "/girls/charlie/album/976065/self-timer/",
    "/girls/dwam/album/975723/parallelism/",
    "/girls/nemesis/album/975237/zilf/",
    "/girls/dwam/album/975049/sun-with-a-moustache/",
    "/girls/dwam/album/994298/boxe-francaise/").map(Uri(_))

  val numberOfPhotoSets = photoSetURIs.length
}