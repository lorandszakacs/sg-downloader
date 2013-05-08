package home.sg.parser

/**
 * @author lorand
 * this class will take the name of the suicide girl and a string corresponding to each of the bellow html tags
 * It parses both type of album info (still in member review and official albums). It then generates relevant
 * information
 *
 * Album still in Member Review:
 * --
 * <div class="preview" id="album_277610" title="Sash: The Grove">
 * <a class="pngSpank" href="/girls/Sash/albums/site/33360/"><img src="/media/albums/0/36/33360/setpreview_medium.jpg" ....." /></a>
 * <div class="date">Apr 10, 2013</div>
 * --
 * image url format: http://img.suicidegirls.com/media/albums/0/36/33360/1505987.jpg
 *
 * ====
 * Pink album:
 * --
 * <div class="preview" id="album_273231" title="Sash: Arboraceous">
 * <a class="pngSpank" href="/girls/Sash/photos/Arboraceous/"><img src="/media/girls/Sash/photos/Arboraceous/setpreview_medium.jpg" ... /></a>
 * <div class="date">Mar 6, 2013</div>
 * --
 * image url format: http://img.suicidegirls.com/media/girls/Sash/photos/Arboraceous/03.jpg
 */
class Album(val sgName: String, previewDiv: String, pngSpankDiv: String, dateDiv: String) {
 
  require(sgName != "")
  require(previewDiv.contains("\"preview\""))
  require(pngSpankDiv.contains("\"pngSpank\""))
  require(dateDiv.contains("\"date\""))

  val date = parseDateDiv(dateDiv)
  val albumName = ""
  val albumNameForLink = ""

  
  private def parsePreviewDiv(preview: String) = {
    
  }
  
  private def parsePngSpankDiv(pngSpank: String) = {
    
  }
  
  private def parseDateDiv(date: String) = {
    //     val stringToNum = Map("Jan" -> "01", "Feb" -> "02", "Mar" -> "03", "Apr" -> "04", "May" -> "05", "Jun" -> "06",
    //      "Jul" -> "07", "Aug" -> "08", "Sep" -> "09", "Oct" -> "10", "Nov" -> "11", "Dec" -> "12")
    //    val monthStr = paramDate.substring(0, 3)
    //    val year = paramDate.substring(paramDate.indexOf(",") + 1)
    //    "%s.%s".format(year, stringToNum(monthStr))
    "NULL"
  }

  private def replacePlus(s: String): String = {
    val sAsListOfStrings = s.toList.map(c => c.toString)
    sAsListOfStrings.map(s => if (s == "+") "%20" else s).mkString
  }
  
    override def equals(that: Any) = {
    that match {
      case f: Album => f.sgName.equals(sgName) && f.date.equals(date) && f.albumName.equals(albumName)
      case _ => false
    }
  }
}