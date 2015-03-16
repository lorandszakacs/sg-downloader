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

import java.time.LocalDate

import scala.io.Source

import com.lorandszakacs.util.html._

import spray.http.Uri

trait PhotoSetPage {
  def html = {
    val resourceName = s"${getClass.getSimpleName().replace("$", "")}.html"
    val URL = getClass.getResource(resourceName)
    val source = Source.fromURL(URL)
    Html(source.getLines().mkString("\n"))
  }

  def uri: Uri
  def numberOfPhotos: Int
  def title: String
  def date: LocalDate
}

object PhotoSetPagePartialDate extends PhotoSetPage {
  def uri: Uri = "https://suicidegirls.com/girls/dwam/album/1239337/adieu-tristesse/"
  def numberOfPhotos: Int = 53
  def title: String = "Adieu Tristesse"
  def date: LocalDate = LocalDate.of(2014, 1, 18)
}

object PhotoSetPageFullDate extends PhotoSetPage {
  def uri: Uri = "https://suicidegirls.com/girls/dwam/album/977051/limportance-d-etre-ernest/"
  def numberOfPhotos: Int = 45
  def title: String = "Limportance d etre Ernest"
  def date: LocalDate = LocalDate.of(2013, 2, 7)
}


