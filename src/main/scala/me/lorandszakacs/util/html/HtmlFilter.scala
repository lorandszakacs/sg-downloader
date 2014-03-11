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

  def apply(doc: Document): Option[List[String]] = {
    val elements = filter(doc)
    val listOfElements = elementsToList(elements)
    if (listOfElements.isEmpty)
      return None
    Some(listOfElements map (_.toString))
  }

  protected final def elementsToList(elements: Elements): List[Element] = {
    val buff = ListBuffer[Element]();
    val iterator = elements.iterator()
    while (iterator.hasNext()) {
      buff.append(iterator.next())
    }
    buff.toList
  }

  def apply(htmlChunk: String): Option[List[String]] = {
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
  override def apply(doc: Document): Option[List[String]] = {
    val resultsFirst = first(doc)
    applySecond(resultsFirst)

  }

  override def apply(htmlChunk: String): Option[List[String]] = {
    val resultsFirst = first(htmlChunk)
    applySecond(resultsFirst)
  }

  private def applySecond(resultsFirst: Option[List[String]]): Option[List[String]] = {
    resultsFirst match {
      case None => None
      case Some(result) => {
        //this new List might contain List(None, None, None,... Some, ...)
        val resultsSecond: List[Option[List[String]]] = result.map(r => second(r))
        val onlySomeResults = resultsSecond filter (option => option.isDefined)
        val results = onlySomeResults map (some => some.get)
        val finalResult = results.flatten
        if (finalResult.isEmpty)
          None
        else
          Some(finalResult)
      }
    }
  }

  override def filter(doc: Document): Elements = null
}

/**
 * @author lorand
 *
 */
case class Attribute(val attribute: String) extends HtmlFilter {
  override def filter(doc: Document): Elements = doc.getElementsByAttribute(attribute)
}

/**
 * @author lorand
 *
 */
case class Class(val className: String) extends HtmlFilter {
  override def filter(doc: Document): Elements = doc.getElementsByClass(className)
}

/**
 * @author lorand
 *
 */
case class Tag(val tagName: String) extends HtmlFilter {
  override def filter(doc: Document): Elements = doc.getElementsByTag(tagName)
}

/**
 * @author lorand
 *
 */
case class HrefLink() extends HtmlFilter {
  override def apply(doc: Document): Option[List[String]] = {
    val hrefAttributes = Value(Attribute("href"))
    hrefAttributes.apply(doc)
  }

  override def filter(doc: Document): Elements = null
}

/*
 * =================================================
 *       Modifiers you apply to other filters
 * =================================================
 */

/**
 * @author lorand
 *
 */
case class RetainFirst(filter: HtmlFilter) extends HtmlFilter {
  override def apply(doc: Document): Option[List[String]] = {
    val allItems = filter.apply(doc)
    allItems match {
      case None => None
      case Some(items) => Some(List(items(0)))
    }
  }
  override def filter(doc: Document): Elements = null
}

/**
 * @author lorand
 *
 */
case class Value(val attribute: Attribute) extends HtmlFilter {

  override def apply(doc: Document): Option[List[String]] = {
    val elementsWithAttribute = elementsToList(filter(doc))
    if (elementsWithAttribute.isEmpty)
      None
    else {
      val attributeContent = elementsWithAttribute map (e => e.attr(attribute.attribute).toString)
      Some(attributeContent.toList)
    }
  }

  override def filter(doc: Document): Elements = doc.getElementsByAttribute(attribute.attribute)
}

/**
 * @author lorand
 *
 */
case class Content(val filter: HtmlFilter) extends HtmlFilter {
  override def apply(doc: Document): Option[List[String]] = {
    val filtered = filter.apply(doc)
    filtered match {
      case None => None
      case Some(items) => {
        val docs = items map (e => Jsoup.parse(e))
        //because we reparse whatever the result was is being put in a new body
        //e.body().children().first() will always return non-null.
        val result = docs map (e => e.body().children().first().html())
        Some(result)
      }
    }
  }

  override def filter(doc: Document): Elements = null
}

