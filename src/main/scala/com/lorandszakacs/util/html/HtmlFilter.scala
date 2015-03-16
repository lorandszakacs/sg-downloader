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
package com.lorandszakacs.util.html

import scala.Option.option2Iterable
import scala.collection.mutable.ListBuffer

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

/**
 * @author Lorand Szakacs, lsz@lorandszakacs.com
 * @since 16 Mar 2015
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
    val buff = ListBuffer[Element]()
    val iterator = elements.iterator()
    while (iterator.hasNext) {
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

private case class CompositeFilter(first: HtmlFilter, second: HtmlFilter) extends HtmlFilter {
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
      case Some(result) =>
        val finalResult = result.flatMap(r => second(r)).flatten
        if (finalResult.isEmpty)
          None
        else
          Some(finalResult)
    }
  }

  override def filter(doc: Document): Elements = null
}

case class Attribute(attribute: String) extends HtmlFilter {
  override def filter(doc: Document): Elements = doc.getElementsByAttribute(attribute)
}

case class Class(className: String) extends HtmlFilter {
  override def filter(doc: Document): Elements = doc.getElementsByClass(className)
}

case class Tag(tagName: String) extends HtmlFilter {
  override def filter(doc: Document): Elements = doc.getElementsByTag(tagName)
}

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

case class Value(attribute: Attribute) extends HtmlFilter {

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

case class Content(filter: HtmlFilter) extends HtmlFilter {
  override def apply(doc: Document): Option[List[String]] = {
    val filtered = filter.apply(doc)
    filtered match {
      case None => None
      case Some(items) =>
        val docs = items map (e => Jsoup.parse(e))
        //because we reparse whatever the result was is being put in a new body
        //e.body().children().first() will always return non-null.
        val result = docs map (e => e.body().children().first().html())
        Some(result)
    }
  }

  override def filter(doc: Document): Elements = null
}

