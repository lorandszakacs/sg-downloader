package me.lorandszakacs.util.html

import java.io.File

import scala.collection.mutable.ListBuffer

import org.jsoup.Jsoup
import org.jsoup.nodes.Element

object HtmlParser {
  def apply(contents: String) = {
    new HtmlParser(contents)
  }

  def apply(file: File) = {

  }

  private final val BetweenQuotesRegex = ".*\"(.*)\".*".r
}

class HtmlParser private (val contents: String) {
  private lazy val document = Jsoup.parse(contents)

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