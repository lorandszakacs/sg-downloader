/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Lorand Szakacs
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
package me.lorandszakacs.util.html

import java.io.File
import scala.collection.mutable.ListBuffer
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

object HtmlParser {
  def apply(contents: String) = {
    new HtmlParser(contents)
  }

  def apply(file: File) = {

  }

  //  re.findall(r'"(.*?)"',string)
  //  private final val BetweenQuotesRegex = ".*\"(.*)\".*".r
  private final val BetweenQuotesRegex = ".*\"(.*)\".*".r
}

class HtmlParser private (val contents: String) {
  private lazy val document = Jsoup.parse(contents)

  def filter(f: HtmlFilter): List[String] = {
    //FIXME: remove redundant tostring
    f.apply(document) map (_.toString)
  }

  def filterByClass(cls: String): List[String] = {
    val elements = document.getElementsByClass(cls)
    val buff = ListBuffer[Element]();
    val iterator = elements.iterator()
    while (iterator.hasNext()) {
      buff.append(iterator.next())
    }
    val result = buff.map(el => el.html()).toList
    result
  }

  def grabAllLinks(): Option[List[String]] = {
    val links = document.getElementsByAttribute("href")
    val buff = ListBuffer[Element]();
    val iterator = links.iterator()
    while (iterator.hasNext()) {
      buff.append(iterator.next())
    }

    if (buff.length == 0)
      None
    else {
      val result = ListBuffer[String]()
      buff.foreach(htmlLink => {
        try {
          val HtmlParser.BetweenQuotesRegex(link) = htmlLink.toString
          result.append(link)
        } catch {
          case e: MatchError => Unit
        }
      })

      if (result.length == 0)
        None
      else
        Some(result.toList)
    }
  }

  def grabFirstLink(): Option[String] = {
    val links = grabAllLinks
    links match {
      case None => None
      case Some(res) => Some(res.head)
    }
  }
}