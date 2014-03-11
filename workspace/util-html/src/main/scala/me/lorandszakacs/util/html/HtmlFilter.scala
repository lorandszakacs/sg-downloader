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

import scala.collection.mutable.ListBuffer

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

/**
 * @author lorand
 *
 */
protected sealed trait HtmlFilter {
  def &&(that: HtmlFilter): HtmlFilter = CompositeFilter(this, that)

  def apply(doc: Document): List[String] = {
    val elements = filter(doc)
    elementsToList(elements) map (_.toString)
  }

  protected final def elementsToList(elements: Elements): List[Element] = {
    val buff = ListBuffer[Element]();
    val iterator = elements.iterator()
    while (iterator.hasNext()) {
      buff.append(iterator.next())
    }
    buff.toList
  }

  def apply(htmlChunk: String): List[String] = {
    val doc = Jsoup.parseBodyFragment(htmlChunk)
    apply(doc)
  }

  protected def filter(doc: Document): Elements
}

/**
 * @author lorand
 *
 */
private case class CompositeFilter(val first: HtmlFilter, val second: HtmlFilter) extends HtmlFilter {
  override def apply(doc: Document): List[String] = {
    (first, second) match {
      case (retainFirst: RetainFirst, normalFilter: HtmlFilter) => {
        val result = normalFilter(doc)
        List(result(0))
      }
      case (normalFilter: HtmlFilter, retainFirst: RetainFirst) => {
        val result = normalFilter(doc)
        List(result(0))
      }
      case (_, _) => {
        val resultsFirst = first(doc)
        val resultsSecond = resultsFirst.map(el => second(el))
        resultsSecond.flatten
      }
    }

  }

  override def apply(htmlChunk: String): List[String] = {
    val resultsFirst = first(htmlChunk)
    val resultsSecond = resultsFirst.map(el => second(el))
    resultsSecond.flatten
  }

  override def filter(doc: Document): Elements = null
}

/**
 * @author lorand
 *
 */
case class RetainFirst() extends HtmlFilter {
  override def filter(doc: Document): Elements = null
}
/**
 * @author lorand
 *
 */
case class Attribute(private val attribute: String) extends HtmlFilter {
  override def filter(doc: Document): Elements = doc.getElementsByAttribute(attribute)
}

case class AttributeContent(private val attribute: String) extends HtmlFilter {
  override def filter(doc: Document): Elements = doc.getElementsByAttribute(attribute)
}

/**
 * @author lorand
 *
 */
case class Class(private val className: String) extends HtmlFilter {
  override def filter(doc: Document): Elements = doc.getElementsByClass(className)
}

/**
 * @author lorand
 *
 */
case class Tag(private val tagName: String) extends HtmlFilter {
  override def filter(doc: Document): Elements = doc.getElementsByTag(tagName)
}

case class HrefLink() extends HtmlFilter {
  private final val BetweenQuotesRegex = "\"(.*?)\"".r

  override def apply(doc: Document): List[String] = {
    val hrefAttributes = filter(doc)
    val elementsWithHref = elementsToList(hrefAttributes)
    val links = elementsWithHref map (e => e.attr("href").toString)
    links.toList
  }

  override def filter(doc: Document): Elements = doc.getElementsByAttribute("href")
}