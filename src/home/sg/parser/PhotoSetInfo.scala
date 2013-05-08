package home.sg.parser

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
class PhotoSetInfo(val sgName: String, previewDiv: String, pngSpankDiv: String, dateDiv: String) {

  require(previewDiv.contains("\"preview\""))
  require(pngSpankDiv.contains("\"pngSpank\""))
  require(dateDiv.contains("\"date\""))

  val date = parseDateDiv(dateDiv)
  val setTitle = parsePreviewDiv(previewDiv)
  val setTitleAsURIPath = parsePngSpankDiv(pngSpankDiv)._2
  val isMR = parsePngSpankDiv(pngSpankDiv)._1

  private def parsePreviewDiv(preview: String) = {
    val titleTag = "title="
    val nameAndTrailingQuote = preview.takeRight(preview.length - preview.indexOf(titleTag) - titleTag.length - sgName.length - 1 - ": ".length)
    nameAndTrailingQuote.take(nameAndTrailingQuote.length - 2).trim()
  }

  private def parsePngSpankDiv(pngSpank: String) = {

    //<a class="pngSpank" href="/girls/Sash/albums/site/33360/"><img src="/media/albums/0/36/33360/setpreview_medium.jpg" ....." /></a>
    def mrParsing() = {
      val srcTag = "img src=\""
      val sufix = "setpreview_medium.jpg\""
      val indexOfSrcTag = pngSpank.indexOf(srcTag)
      val indexOfSecondQuote = pngSpank.indexOf("\"", indexOfSrcTag + srcTag.length)
      val albumPathPlusSufix = pngSpank.substring(indexOfSrcTag + srcTag.length, indexOfSecondQuote)
      replacePlus(albumPathPlusSufix.take(albumPathPlusSufix.length() - sufix.length() + 1))
    }

    //<a class="pngSpank" href="/girls/Sash/photos/Arboraceous/"><img src="/media/girls/Sash/photos/Arboraceous/setpreview_medium.jpg" ... /></a>
    def nonMrParsing() = {
      val hrefTag = "href=\""
      val indexOfHrefTag = pngSpank.indexOf(hrefTag)
      val indexOfSecondQuote = pngSpank.indexOf("\"", indexOfHrefTag + hrefTag.length)
      replacePlus(pngSpank.substring(indexOfHrefTag + hrefTag.length, indexOfSecondQuote))
    }

    val isMR = pngSpank.contains("albums/site/");
    (isMR, if (isMR) mrParsing() else nonMrParsing())
  }

  //<div class="date">Apr 10, 2013</div>
  private def parseDateDiv(date: String) = {
    val stringToNum = Map("Jan" -> "01", "Feb" -> "02", "Mar" -> "03", "Apr" -> "04", "May" -> "05", "Jun" -> "06",
      "Jul" -> "07", "Aug" -> "08", "Sep" -> "09", "Oct" -> "10", "Nov" -> "11", "Dec" -> "12")
    val onlyDivAtEnd = date.takeRight(date.length - date.indexOf(">") - 1);
    val onlyDate = onlyDivAtEnd.substring(0, onlyDivAtEnd.indexOf("</div>"))
    val monthStr = onlyDate.substring(0, 3)
    val year = onlyDate.substring(onlyDate.indexOf(",") + 2)
    "%s.%s".format(year, stringToNum(monthStr))
  }

  private def replacePlus(s: String): String = {
    val sAsListOfStrings = s.toList.map(c => c.toString)
    sAsListOfStrings.map(s => if (s == "+") "%20" else s).mkString
  }

  override def equals(that: Any) = {
    that match {
      case f: PhotoSetInfo => f.sgName.equals(sgName) && f.date.equals(date) && f.setTitle.equals(setTitle)
      case _ => false
    }
  }
}