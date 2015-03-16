/**
 * Copyright 2015 Lorand Szakacs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.lorandszakacs.sgd.client.data

import scala.io.Source
import com.lorandszakacs.util.html.Html
import spray.http.Uri

/**
 * @author Lorand Szakacs, lsz@lorandszakacs.com
 * @since 16 Mar 2015
 *
 */
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