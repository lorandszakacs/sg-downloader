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

import com.lorandszakacs.sg.model.ModelName
import com.lorandszakacs.util.html.Html

import scala.io.Source

/**
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 16 Mar 2015
  *
  */
object SGProfileListPage {
  val names: List[ModelName] = List(
    "Sash",
    "Kemper",
    "Radeo",
    "Lass",
    "Quinne",
    "Riae",
    "Dimples",
    "Mel",
    "Phecda",
    "Annalee",
    "Bully",
    "Rambo"
  )

  val numberOfSGs = names.length

  def html = {
    val resourceName = s"${getClass.getSimpleName.replace("$", "")}.html"
    val URL = getClass.getResource(resourceName)
    val source = Source.fromURL(URL)
    Html(source.getLines().mkString("\n"))
  }
}

object HopefulProfileListPage {
  val names: List[ModelName] = List(
    "Nuru",
    "Lain_",
    "Drica",
    "River_",
    "Mayrose",
    "Rubytrue",
    "Alekat96",
    "_effy_",
    "Lenka87",
    "Davieduke",
    "Vikat",
    "Foxynova"
  )

  val numberOfSGs = names.length

  def html = {
    val resourceName = s"${getClass.getSimpleName.replace("$", "")}.html"
    val URL = getClass.getResource(resourceName)
    val source = Source.fromURL(URL)
    Html(source.getLines().mkString("\n"))
  }
}