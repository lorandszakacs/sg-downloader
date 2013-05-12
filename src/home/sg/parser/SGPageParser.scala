package home.sg.parser
import scala.io.Source
import home.sg.util.IO

/**
 * @author lorand
 *
 *
 * "<div class=\"preview\" id=\"album_277610\" title=\"Sash: The Grove\">",
 * "<a class=\"pngSpank\" href=\"/girls/Sash/albums/site/33360/\"><img src=\"/media/albums/0/36/33360/setpreview_medium.jpg\" width=\"169\" height=\"104\" alt=\"SuicideGirls: The Grove\" /></a>",
 * "<div class=\"date\">Apr 10, 2013</div>"
 *
 */
object SGPageParser {

  /**
   * @param pageStream the function will close the stream
   * @return
   */
  def parseSetAlbumPageToSetHeaders(sgName: String, pageStream: Source): List[PhotoSetHeader] = {
    def isPreview(s: String) = s.contains("<div class=\"preview\"")
    def isPngSpank(s: String) = s.contains("<a class=\"pngSpank\"")
    def isDate(s: String) = s.contains("<div class=\"date\"")
    def isRelevant(s: String) = {
      val str = s.trim()
      isPreview(str) || isPngSpank(str) || isDate(str)
    }

    val tempStrings = pageStream.getLines
    //nasty hack to preserve the spaces in the 
    val page = tempStrings.mkString("\n---")
    val strings = page.split("\n---").toList

    val remaining = strings.filter(isRelevant).toList

    assume(remaining.length > 0)
    assume(remaining.length % 3 == 0, "the number of lines filtered from the set album page was not a multiple of 3")
    assume(isPreview(remaining(0).trim), "the first string in a 3 tuple is not the preview")
    assume(isPngSpank(remaining(1).trim), "the second string in a 3 tuple is not the pngSpank")
    assume(isDate(remaining(2).trim), "the third string in a 3 tuple is not the date")

    val range = 3 to remaining.length by 3
    val htmlThreeTuples = for (n <- range) yield {
      val threeTupleAsList = remaining.slice(n - 3, n)
      (threeTupleAsList(0), threeTupleAsList(1), threeTupleAsList(2))
    }
    pageStream.close
    val returnV = htmlThreeTuples map { threeTuple => new PhotoSetHeader(sgName, threeTuple._1, threeTuple._2, threeTuple._3) }
    returnV.toList.sortBy(_.relativeSaveLocation)
  }

  def parseSetPageToImageURLs(pageStream: Source): List[String] = {
    def computeStartIndex(s: String) = {
      val startString = "http://img.suicidegirls.com"
      s.indexOf(startString)
    }

    def computeEndIndex(s: String) = {
      val endString = "jpg\""
      s.indexOf(endString) + endString.length - 1
    }

    def replaceSpaces(s: String) = {
      val sAsListOfStrings = s.toList.map(c => c.toString)
      sAsListOfStrings.map(s => if (s == " ") "%20" else s).mkString
    }

    def pagesWithImageHolder() = {
      val imageLines = pageStream.getLines.filter(_.contains("ImageHolder"))
      val result = imageLines.toList map (s => replaceSpaces(s.substring(computeStartIndex(s), computeEndIndex(s))))
      pageStream.close
      result
    }

    pagesWithImageHolder()
  }
}