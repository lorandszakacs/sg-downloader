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

import akka.http.scaladsl.model.Uri
import com.github.nscala_time.time.Imports._
import com.lorandszakacs.sg.model.PhotoSetTitle
import com.lorandszakacs.util.html._

import scala.io.Source

/**
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 16 Mar 2015
  *
  */
trait PhotoSetPage {
  def html = {
    val resourceName = s"${getClass.getSimpleName.replace("$", "")}.txt"
    val URL = getClass.getResource(resourceName)
    val source = Source.fromURL(URL)
    Html(source.getLines().mkString("\n"))
  }

  def currentYear: Int = DateTime.now(DateTimeZone.UTC).getYear

  def uri: Uri

  def numberOfPhotos: Int

  def title: PhotoSetTitle

  def date: LocalDate
}

object PhotoSetPagePartialDate extends PhotoSetPage {
  def uri: Uri = "https://suicidegirls.com/girls/dwam/album/1239337/adieu-tristesse/"

  def numberOfPhotos: Int = 53

  def title: PhotoSetTitle = "ADIEU TRISTESSE"

  def date: LocalDate = LocalDate.parse("2015-01-18").withYear(currentYear)
}

object PhotoSetPageFullDate extends PhotoSetPage {
  def uri: Uri = "https://suicidegirls.com/girls/dwam/album/977051/limportance-d-etre-ernest/"

  def numberOfPhotos: Int = 45

  def title: PhotoSetTitle = "LIMPORTANCE D ETRE ERNEST"

  def date: LocalDate = LocalDate.parse("2013-02-07")
}


