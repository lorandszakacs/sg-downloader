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
package home.sg.parser.html

import home.sg.util.IO
import scala.collection.immutable.StringOps

/**
 * @author lorand
 */
class PhotoSet(header: PhotoSetHeader, val imageURLs: List[String]) {

  val sgName: String = header.sgName
  val title: String = header.title

  val relativeSaveLocation: String = header.relativeSaveLocation

  val URLSaveLocationPairs: List[(String, String)] = imageURLs.zip((1 to imageURLs.length).map(imageSaveLocation))

  override def toString: String = {
    val result = URLSaveLocationPairs.map(p => "  " + p._2 + "     =>     " + p._1)
    result.mkString("\n")
  }

  private def imageSaveLocation(i: Int) = IO.concatPath(relativeSaveLocation, "%02d.jpg".format(i))
}

/**
 * @author lorand
 *
 * Abstract representation of a PhotoSetHeader. This is the main
 * way to identify any individual set.
 */
sealed trait PhotoSetHeader {

  val sgName: String

  /**
   * represented as a string:
   *   yyyy.mm
   */
  val date: String

  /**
   * the humanly readable title of this set
   */
  val title: String

  /**
   * $sgName/$date - $setTitle
   */
  final lazy val relativeSaveLocation: String = "%s/%s".format(sgName, fileSystemSetTitle)

  /**
   * The URL for this set
   */
  final lazy val URL = "http://suicidegirls.com%s".format(partialURL)

  /**
   * Overridden in the subclass, it's the URL of the set
   * without the starting suicidegirls.com
   */
  protected val partialURL: String;

  private lazy val fileSystemSetTitle = "%s - %s".format(date, title)

  override def equals(that: Any) = {
    that match {
      case f: PhotoSetHeader => f.sgName.equals(sgName) && f.date.equals(date) && f.title.equals(title)
      case _ => false
    }
  }

  protected final val stringToNum = Map("Jan" -> "01", "Feb" -> "02", "Mar" -> "03", "Apr" -> "04", "May" -> "05", "Jun" -> "06",
    "Jul" -> "07", "Aug" -> "08", "Sep" -> "09", "Oct" -> "10", "Nov" -> "11", "Dec" -> "12")

  protected final def between(start: String, end: String, string: String): String = {
    val startIndex = string.indexOf(start) + start.length
    val endIndex = string.indexOf(end)
    string.substring(startIndex, endIndex)
  }

  /**
   * @return "The  Grove" -> "The Grove"
   */
  protected final def trimSeparatorSequencesOfSpaces(s: String): String = {
    val words = s.split(" ")
    val trimmedWords = words.map(_.trim()).filterNot(_ == "")
    trimmedWords.mkString(" ");
  }

  override def toString: String = "%s  @  %s".format(relativeSaveLocation, URL)
}

object PhotoSetHeader {
  def apply(sgName: String, previewDiv: String, pngSpankDiv: String, dateDiv: String): PhotoSetHeader =
    new SGPhotoSetHeader(sgName, previewDiv, pngSpankDiv, dateDiv);

  def apply(sgName: String, headerWithNameAndURL: String, lineWithDate: String): PhotoSetHeader =
    new HopefulPhotoSetHeader(sgName, headerWithNameAndURL, lineWithDate)

}

/**
 * @author lorand
 * this class will take the name of the suicide girl and a string corresponding to each of the bellow html tags
 * It parses both type of album info (still in member review and official albums). It then generates relevant
 * information
 *
 * Set still in Member Review:
 * --
 * <div class="preview" id="album_277610" title="Sash: The Grove">
 * <a class="pngSpank" href="/girls/Sash/albums/site/33360/"><img src="/media/albums/0/36/33360/setpreview_medium.jpg" ....." /></a>
 * <div class="date">Apr 10, 2013</div>
 * --
 * image url format:
 *   http://img.suicidegirls.com/media/albums/0/36/33360/1505987.jpg
 *
 * ====
 * Pink set:
 * --
 * <div class="preview" id="album_273231" title="Sash: Arboraceous">
 * <a class="pngSpank" href="/girls/Sash/photos/Arboraceous/"><img src="/media/girls/Sash/photos/Arboraceous/setpreview_medium.jpg" ... /></a>
 * <div class="date">Mar 6, 2013</div>
 * --
 * image url format:
 *   http://img.suicidegirls.com/media/girls/Sash/photos/Arboraceous/03.jpg
 *
 * ====
 * Pink set with spaces in the name:
 *  <div class="preview" id="album_271379" title="Nahp:   Girl Next Door">
 *  <a class="pngSpank" href="/girls/Nahp/photos/++Girl+Next+Door/"><img src="/media/girls/Nahp/photos/  Girl Next Door/setpreview_medium.jpg" ... /></a>
 * 	<div class="date">Jan 1, 2013</div>
 *  --
 * image url format:
 *   http://img.suicidegirls.com/media/girls/Nahp/photos/%20%20Girl%20Next%20Door/01.jpg
 */
private class SGPhotoSetHeader(override val sgName: String, previewDiv: String, pngSpankDiv: String, dateDiv: String) extends PhotoSetHeader {

  require(previewDiv.contains("\"preview\""))
  require(pngSpankDiv.contains("\"pngSpank\""))
  require(dateDiv.contains("\"date\""))

  override val date = parseDateDiv(dateDiv)
  override val title = trimSeparatorSequencesOfSpaces(parsePreviewDiv(previewDiv))

  protected override val partialURL = parsePngSpankDiv(pngSpankDiv)

  private def parsePreviewDiv(preview: String) = {
    val titleTag = "title="
    val nameAndTrailingQuote = preview.takeRight(preview.length - preview.indexOf(titleTag) - titleTag.length - sgName.length - 1 - ": ".length)
    nameAndTrailingQuote.take(nameAndTrailingQuote.length - 2).trim()
  }

  private def parsePngSpankDiv(pngSpank: String): String = {
    //<a class="pngSpank" href="/members/Sash/albums/site/33360/"/><img src="/media/albums/0/36/33360/setpreview_medium.jpg" ....
    //actual link: http://suicidegirls.com/members/Sash/albums/site/33360/

    //<a class="pngSpank" href="/girls/Sash/photos/Arboraceous/"><img src="/media/girls/Sash/photos/Arboraceous/setpreview_medium.jpg" ... /></a>
    //actual link:
    val hrefTag = "href=\""
    val indexOfHrefTag = pngSpank.indexOf(hrefTag)
    val indexOfSecondQuote = pngSpank.indexOf("\"", indexOfHrefTag + hrefTag.length)
    pngSpank.substring(indexOfHrefTag + hrefTag.length, indexOfSecondQuote)
  }

  //<div class="date">Apr 10, 2013</div>
  private def parseDateDiv(date: String): String = {
    val onlyDivAtEnd = date.takeRight(date.length - date.indexOf(">") - 1);
    val onlyDate = onlyDivAtEnd.substring(0, onlyDivAtEnd.indexOf("</div>"))
    val monthStr = onlyDate.substring(0, 3)
    val year = onlyDate.substring(onlyDate.indexOf(",") + 2)
    "%s.%s".format(year, stringToNum(monthStr))
  }
}

private class HopefulPhotoSetHeader(override val sgName: String, headerWithNameAndURL: String, lineWithDate: String) extends PhotoSetHeader {
  require(headerWithNameAndURL.contains("<h1><a href="))
  require(lineWithDate.contains("UP SINCE:"))

  override val date = parseDate(lineWithDate)
  override val title = trimSeparatorSequencesOfSpaces(parseHeaderWithNameAndURL(headerWithNameAndURL)._1)
  protected override val partialURL = parseHeaderWithNameAndURL(headerWithNameAndURL)._2

  //<span class="prefix" title="This set is in the Member Review section. Members can see and comment on it.">UP SINCE:</span> Mar 23 2013				</p>)
  private def parseDate(line: String) = {
    val UpSince = "UP SINCE:</span>"
    val dateWithTrailingSpace = line.drop(line.indexOf(UpSince) + UpSince.length + 1)
    val monthAsString = dateWithTrailingSpace.take(3)
    val year = dateWithTrailingSpace.drop(4).toList.dropWhile(_ != ' ').drop(1).take(4).mkString
    "%s.%s".format(year, stringToNum(monthAsString))
  }

  //<h1><a href="/members/Dalmasca/albums/site/33209/">Picker-Uppers</a></h1>
  private def parseHeaderWithNameAndURL(header: String) = {
    val albumURL1 = header.dropWhile(_ != '\"').drop(1)
    val albumURL = albumURL1.takeWhile(_ != '\"')
    val name = between("/\">", "</a></h1>", header)
    (name, albumURL)
  }
}