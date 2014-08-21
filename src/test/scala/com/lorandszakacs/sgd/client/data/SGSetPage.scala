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