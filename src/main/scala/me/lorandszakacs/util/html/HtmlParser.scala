package me.lorandszakacs.util.html

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import scala.collection.mutable.ListBuffer
import org.jsoup.nodes.Element
import java.io.File

object HtmlParser {
  def apply(contents: String) = {
    new HtmlParser(contents)
  }

  def apply(file: File) = {

  }

  private final val BetweenQuotesRegex = ".*\"(.*)\".*".r
}

class HtmlParser private (val contents: String) {
  private lazy val document: Document = Jsoup.parse(contents)

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

  def grabFirstLink(): Option[String] = {
    val links = document.getElementsByAttribute("href")
    val buff = ListBuffer[Element]();
    val iterator = links.iterator()
    while (iterator.hasNext()) {
      buff.append(iterator.next())
    }
    if (buff.length == 0)
      None
    else {
      val firstLink: String = buff(0).toString()
      try {
        val HtmlParser.BetweenQuotesRegex(result) = firstLink
        Some(result)
      } catch {
        case e: MatchError => None
      }
    }
  }
}