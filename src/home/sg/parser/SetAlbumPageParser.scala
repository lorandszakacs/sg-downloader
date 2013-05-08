package home.sg.parser
import scala.io.Source

/**
 * @author lorand
 *
 *
 * "<div class=\"preview\" id=\"album_277610\" title=\"Sash: The Grove\">",
 * "<a class=\"pngSpank\" href=\"/girls/Sash/albums/site/33360/\"><img src=\"/media/albums/0/36/33360/setpreview_medium.jpg\" width=\"169\" height=\"104\" alt=\"SuicideGirls: The Grove\" /></a>",
 * "<div class=\"date\">Apr 10, 2013</div>"
 *
 */
object SGSetAlbumPageParser {

  /**
 * @param pageStream the function will close the stream
 * @return
 */
def parseSetAlbumPage(pageStream: Source): List[(String, String, String)] = {
    def isPreview(s: String) = s.startsWith("<div class=\"preview\"")
    def isPngSpank(s: String) = s.startsWith("<a class=\"pngSpank\"")
    def isDate(s: String) = s.startsWith("<div class=\"date\"")
    def isRelevant(s: String) = {
      val str = s.trim()
      isPreview(str) || isPngSpank(str) || isDate(str)
    }

    val strings = pageStream.getLines
    val remaining = strings.filter(isRelevant).toList

    assume(remaining.length % 3 == 0, "the number of lines filtered from the set album page was not a multiple of 3")
    assume(isPreview(remaining(0).trim), "the first string in a 3 tuple is not the preview")
    assume(isPngSpank(remaining(1).trim), "the second string in a 3 tuple is not the pngSpank")
    assume(isDate(remaining(2).trim), "the third string in a 3 tuple is not the date")

    val range = 3 to remaining.length by 3
    val returnVal = for (n <- range) yield {
      val threeTupleAsList = remaining.slice(n - 3, n)
      (threeTupleAsList(0), threeTupleAsList(1), threeTupleAsList(2))
    }
    pageStream.close
    returnVal.toList
  }
}