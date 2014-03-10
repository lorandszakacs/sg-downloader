package me.lorandszakacs.util.html

import scala.collection.mutable.ListBuffer

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

sealed trait HtmlFilter {
  def &&(first: HtmlFilter, second: HtmlFilter): HtmlFilter = CompositeFilter(first, second)

  def apply(doc: Document): List[Element]

  def apply(htmlChunk: String): List[Element]
}

private case class CompositeFilter(val first: HtmlFilter, val second: HtmlFilter) extends HtmlFilter {

  override def apply(doc: Document): List[Element] = {
    val resultsFirst = first(doc)
    val resultsSecond = resultsFirst.map(el => second(el.html()))
    resultsSecond.flatten
  }

  override def apply(htmlChunk: String): List[Element] = {
    val resultsFirst = first(htmlChunk)
    val resultsSecond = resultsFirst.map(el => second(el.html()))
    resultsSecond.flatten
  }
}

case class Attribute(private val attribute: String) extends HtmlFilter {

  override def apply(doc: Document): List[Element] = {
    val elements = doc.getElementsByAttribute(attribute)
    val buff = ListBuffer[Element]();
    val iterator = elements.iterator()
    while (iterator.hasNext()) {
      buff.append(iterator.next())
    }
    buff.toList
  }

  override def apply(htmlChunk: String): List[Element] = {
    val doc = Jsoup.parseBodyFragment(htmlChunk)
    this.apply(doc)
  }
}

case class Class(private val className: String) extends HtmlFilter {
  override def apply(doc: Document): List[Element] = {
    val elements = doc.getElementsByClass(className)
    val buff = ListBuffer[Element]();
    val iterator = elements.iterator()
    while (iterator.hasNext()) {
      buff.append(iterator.next())
    }
    buff.toList
  }

  override def apply(htmlChunk: String): List[Element] = {
    val doc = Jsoup.parseBodyFragment(htmlChunk)
    this.apply(doc)
  }
}