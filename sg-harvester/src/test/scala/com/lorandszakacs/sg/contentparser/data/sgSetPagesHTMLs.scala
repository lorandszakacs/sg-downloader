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
package com.lorandszakacs.sg.contentparser.data

import com.lorandszakacs.util.time._
import com.lorandszakacs.sg._
import com.lorandszakacs.sg.model.PhotoSet
import com.lorandszakacs.util.html.Html

import scala.io.Source

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
object SGSetPageAllInPast extends URLConversions with TestDataUtil {

  val photoSets: List[PhotoSet] = List(
    PhotoSet(
      url   = s"${core.Domain}/girls/dwam/album/1239337/adieu-tristesse/",
      title = "ADIEU TRISTESSE",
      //because this set has a simplified date, it is important that in the test set this is always the current year
      date   = LocalDate.parse(s"${unsafeCurrentYear()}-01-18"),
      photos = List.empty
    ),
    PhotoSet(
      url    = s"${core.Domain}/girls/dwam/album/977051/limportance-d-etre-ernest/",
      title  = "LIMPORTANCE D ETRE ERNEST",
      date   = LocalDate.parse("2013-02-07"),
      photos = List.empty
    ),
    PhotoSet(
      url    = s"${core.Domain}/girls/dwam/album/976671/midsummer-crown/",
      title  = "MIDSUMMER CROWN",
      date   = LocalDate.parse("2012-08-01"),
      photos = List.empty
    ),
    PhotoSet(
      url    = s"${core.Domain}/girls/dwam/album/976285/woad/",
      title  = "WOAD",
      date   = LocalDate.parse("2012-02-09"),
      photos = List.empty
    ),
    PhotoSet(
      url    = s"${core.Domain}/girls/charlie/album/976065/self-timer/",
      title  = "SELF TIMER",
      date   = LocalDate.parse("2011-10-27"),
      photos = List.empty
    ),
    PhotoSet(
      url    = s"${core.Domain}/girls/dwam/album/975723/parallelism/",
      title  = "PARALLELISM",
      date   = LocalDate.parse("2011-05-19"),
      photos = List.empty
    ),
    PhotoSet(
      url    = s"${core.Domain}/girls/nemesis/album/975237/zilf/",
      title  = "ZILF",
      date   = LocalDate.parse("2010-10-30"),
      photos = List.empty
    ),
    PhotoSet(
      url    = s"${core.Domain}/girls/dwam/album/975049/sun-with-a-moustache/",
      title  = "SUN WITH A MOUSTACHE",
      date   = LocalDate.parse("2010-07-01"),
      photos = List.empty
    ),
    PhotoSet(
      url    = s"${core.Domain}/girls/dwam/album/994298/boxe-francaise/",
      title  = "BOXE FRANCAISE",
      date   = LocalDate.parse("2010-05-22"),
      photos = List.empty
    )
  )

  val numberOfPhotoSets: Int = photoSets.length

  def html: Html = {
    val resourceName = s"${getClass.getSimpleName.replace("$", "")}.txt"
    val URL          = getClass.getResource(resourceName)
    val source       = Source.fromURL(URL)
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
object SGSetPageSomeInPast extends URLConversions with TestDataUtil {

  val photoSets: List[PhotoSet] = List(
    PhotoSet(
      url   = s"${core.Domain}/girls/moon/album/2696495/two-moons/",
      title = "TWO MOONS",
      date  = LocalDate.parse(s"${unsafeCurrentYear()}-05-26")
    ),
    PhotoSet(
      url   = s"${core.Domain}/girls/moon/album/2480614/moonlight/",
      title = "MOONLIGHT",
      date  = LocalDate.parse(s"${unsafeCurrentYear()}-02-05")
    ),
    PhotoSet(
      url   = s"${core.Domain}/girls/moon/album/2264858/ramble-on/",
      title = "RAMBLE ON",
      date  = LocalDate.parse("2015-09-29")
    ),
    PhotoSet(
      url   = s"${core.Domain}/girls/moon/album/1835908/seduction/",
      title = "SEDUCTION",
      date  = LocalDate.parse("2015-03-03")
    ),
    PhotoSet(
      url   = s"${core.Domain}/girls/moon/album/1585654/little-lies/",
      title = "LITTLE LIES",
      date  = LocalDate.parse("2014-11-18")
    ),
    PhotoSet(
      url   = s"${core.Domain}/girls/moon/album/1437765/spring-cleaning/",
      title = "SPRING CLEANING",
      date  = LocalDate.parse("2014-08-26")
    ),
    PhotoSet(
      url   = s"${core.Domain}/girls/moon/album/1395396/moon-spells/",
      title = "MOON SPELLS",
      date  = LocalDate.parse("2014-08-05")
    ),
    PhotoSet(
      url   = s"${core.Domain}/girls/moon/album/1289210/wake-up-slow/",
      title = "WAKE UP SLOW",
      date  = LocalDate.parse("2014-03-21")
    ),
    PhotoSet(
      url   = s"${core.Domain}/girls/moon/album/997826/mirage/",
      title = "MIRAGE",
      date  = LocalDate.parse("2013-12-29")
    )
  )

  val numberOfPhotoSets: Int = photoSets.length

  def html: Html = {
    val resourceName = s"${getClass.getSimpleName.replace("$", "")}.txt"
    val URL          = getClass.getResource(resourceName)
    val source       = Source.fromURL(URL)
    Html(source.getLines().mkString("\n"))
  }
}
