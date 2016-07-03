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
package com.lorandszakacs.sg.crawler.page.data

import akka.http.scaladsl.model.Uri
import com.lorandszakacs.sg.model.PhotoSet
import org.joda.time.LocalDate

import scala.io.Source
import com.lorandszakacs.util.html.Html

/**
  *
  * Should have same name as this file:
  *
  * com/lorandszakacs/sg/crawler/page/data/SGSetPageAllInPast.html
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 16 Mar 2015
  *
  */
object SGSetPageAllInPast {
  val photoSets: List[PhotoSet] = List(
    PhotoSet(
      uri = "/girls/dwam/album/1239337/adieu-tristesse/",
      title = "",
      date = new LocalDate(2003, 2, 2)
    ),
    PhotoSet(
      uri = "/girls/dwam/album/977051/limportance-d-etre-ernest/",
      title = "",
      date = new LocalDate(2003, 2, 2)
    ),
    PhotoSet(
      uri = "/girls/dwam/album/976671/midsummer-crown/",
      title = "",
      date = new LocalDate(2003, 2, 2)
    ),
    PhotoSet(
      uri = "/girls/dwam/album/976285/woad/",
      title = "",
      date = new LocalDate(2003, 2, 2)
    ),
    PhotoSet(
      uri = "/girls/charlie/album/976065/self-timer/",
      title = "",
      date = new LocalDate(2003, 2, 2)
    ),
    PhotoSet(
      uri = "/girls/dwam/album/975723/parallelism/",
      title = "",
      date = new LocalDate(2003, 2, 2)
    ),
    PhotoSet(
      uri = "/girls/nemesis/album/975237/zilf/",
      title = "",
      date = new LocalDate(2003, 2, 2)
    ),
    PhotoSet(
      uri = "/girls/dwam/album/975049/sun-with-a-moustache/",
      title = "",
      date = new LocalDate(2003, 2, 2)
    ),
    PhotoSet(
      uri = "/girls/dwam/album/994298/boxe-francaise/",
      title = "",
      date = new LocalDate(2003, 2, 2)
    )
  )

  val photoSetURIs = photoSets.map(ph => Uri(ph.uri))
  val numberOfPhotoSets = photoSetURIs.length

  def html = {
    val resourceName = s"${getClass.getSimpleName.replace("$", "")}.html"
    val URL = getClass.getResource(resourceName)
    val source = Source.fromURL(URL)
    Html(source.getLines().mkString("\n"))
  }
}


/**
  *
  * Should have same name as this file:
  *
  * com/lorandszakacs/sg/crawler/page/data/SGSetPageAllInPast.html
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 03 Jul 2016
  *
  */
object SGSetPageSomeInPast {
  val photoSets = List(
    PhotoSet(
      uri = "/girls/moon/album/2696495/two-moons/",
      title = "",
      date = new LocalDate(2003, 2, 2)
    ),
    PhotoSet(
      uri = "/girls/moon/album/2480614/moonlight/",
      title = "",
      date = new LocalDate(2003, 2, 2)
    ),
    PhotoSet(
      uri = "/girls/moon/album/2264858/ramble-on/",
      title = "",
      date = new LocalDate(2003, 2, 2)
    ),
    PhotoSet(
      uri = "/girls/moon/album/1835908/seduction/",
      title = "",
      date = new LocalDate(2003, 2, 2)
    ),
    PhotoSet(
      uri = "/girls/moon/album/1585654/little-lies/",
      title = "",
      date = new LocalDate(2003, 2, 2)
    ),
    PhotoSet(
      uri = "/girls/moon/album/1437765/spring-cleaning/",
      title = "",
      date = new LocalDate(2003, 2, 2)
    ),
    PhotoSet(
      uri = "/girls/moon/album/1395396/moon-spells/",
      title = "",
      date = new LocalDate(2003, 2, 2)
    ),
    PhotoSet(
      uri = "/girls/moon/album/1289210/wake-up-slow/",
      title = "",
      date = new LocalDate(2003, 2, 2)
    ),
    PhotoSet(
      uri = "/girls/moon/album/997826/mirage/",
      title = "",
      date = new LocalDate(2003, 2, 2)
    )
  )

  val photoSetURIs = photoSets.map(ph => Uri(ph.uri))
  val numberOfPhotoSets = photoSetURIs.length

  def html = {
    val resourceName = s"${getClass.getSimpleName.replace("$", "")}.html"
    val URL = getClass.getResource(resourceName)
    val source = Source.fromURL(URL)
    Html(source.getLines().mkString("\n"))
  }
}

