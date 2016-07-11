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
package com.lorandszakacs.sg.crawler.impl.data

import com.lorandszakacs.sg.URLConversions
import com.lorandszakacs.sg.model.PhotoSet
import com.lorandszakacs.util.html.Html
import org.joda.time.LocalDate

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
object SGSetPageAllInPast extends URLConversions {
  val photoSets: List[PhotoSet] = List(
    PhotoSet(
      url = "https://www.suicidegirls.com/girls/dwam/album/1239337/adieu-tristesse/",
      title = "ADIEU TRISTESSE",
      date = new LocalDate("2016-01-18")
    ),
    PhotoSet(
      url = "https://www.suicidegirls.com/girls/dwam/album/977051/limportance-d-etre-ernest/",
      title = "LIMPORTANCE D ETRE ERNEST",
      date = new LocalDate("2013-02-07")
    ),
    PhotoSet(
      url = "https://www.suicidegirls.com/girls/dwam/album/976671/midsummer-crown/",
      title = "MIDSUMMER CROWN",
      date = new LocalDate("2012-08-01")
    ),
    PhotoSet(
      url = "https://www.suicidegirls.com/girls/dwam/album/976285/woad/",
      title = "WOAD",
      date = new LocalDate("2012-02-09")
    ),
    PhotoSet(
      url = "https://www.suicidegirls.com/girls/charlie/album/976065/self-timer/",
      title = "SELF TIMER",
      date = new LocalDate("2011-10-27")
    ),
    PhotoSet(
      url = "https://www.suicidegirls.com/girls/dwam/album/975723/parallelism/",
      title = "PARALLELISM",
      date = new LocalDate("2011-05-19")
    ),
    PhotoSet(
      url = "https://www.suicidegirls.com/girls/nemesis/album/975237/zilf/",
      title = "ZILF",
      date = new LocalDate("2010-10-30")
    ),
    PhotoSet(
      url = "https://www.suicidegirls.com/girls/dwam/album/975049/sun-with-a-moustache/",
      title = "SUN WITH A MOUSTACHE",
      date = new LocalDate("2010-07-01")
    ),
    PhotoSet(
      url = "https://www.suicidegirls.com/girls/dwam/album/994298/boxe-francaise/",
      title = "BOXE FRANCAISE",
      date = new LocalDate("2010-05-22")
    )
  )

  val numberOfPhotoSets = photoSets.length

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
object SGSetPageSomeInPast extends URLConversions {
  val photoSets: List[PhotoSet] = List(
    PhotoSet(
      url = "https://www.suicidegirls.com/girls/moon/album/2696495/two-moons/",
      title = "TWO MOONS",
      date = new LocalDate("2016-05-26")
    ),
    PhotoSet(
      url = "https://www.suicidegirls.com/girls/moon/album/2480614/moonlight/",
      title = "MOONLIGHT",
      date = new LocalDate("2016-02-05")
    ),
    PhotoSet(
      url = "https://www.suicidegirls.com/girls/moon/album/2264858/ramble-on/",
      title = "RAMBLE ON",
      date = new LocalDate("2015-09-29")
    ),
    PhotoSet(
      url = "https://www.suicidegirls.com/girls/moon/album/1835908/seduction/",
      title = "SEDUCTION",
      date = new LocalDate("2015-03-03")
    ),
    PhotoSet(
      url = "https://www.suicidegirls.com/girls/moon/album/1585654/little-lies/",
      title = "LITTLE LIES",
      date = new LocalDate("2014-11-18")
    ),
    PhotoSet(
      url = "https://www.suicidegirls.com/girls/moon/album/1437765/spring-cleaning/",
      title = "SPRING CLEANING",
      date = new LocalDate("2014-08-26")
    ),
    PhotoSet(
      url = "https://www.suicidegirls.com/girls/moon/album/1395396/moon-spells/",
      title = "MOON SPELLS",
      date = new LocalDate("2014-08-05")
    ),
    PhotoSet(
      url = "https://www.suicidegirls.com/girls/moon/album/1289210/wake-up-slow/",
      title = "WAKE UP SLOW",
      date = new LocalDate("2014-03-21")
    ),
    PhotoSet(
      url = "https://www.suicidegirls.com/girls/moon/album/997826/mirage/",
      title = "MIRAGE",
      date = new LocalDate("2013-12-29")
    )
  )

  val numberOfPhotoSets = photoSets.length

  def html = {
    val resourceName = s"${getClass.getSimpleName.replace("$", "")}.html"
    val URL = getClass.getResource(resourceName)
    val source = Source.fromURL(URL)
    Html(source.getLines().mkString("\n"))
  }
}

