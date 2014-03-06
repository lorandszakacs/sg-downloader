package me.lorandszakacs.util.html

import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist
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
}

class HtmlParser private (val contents: String) {
  private lazy val document: Document = Jsoup.parse(contents)

  def filterByClass(cls: String): List[String] = {
    val elements = document.getElementsByClass(cls)
    val iterator = elements.iterator()
    val buff = ListBuffer[Element]();
    while (iterator.hasNext()) {
      buff.append(iterator.next())
    }
    val result = buff.map(el => el.html()).toList
    result
  }

}