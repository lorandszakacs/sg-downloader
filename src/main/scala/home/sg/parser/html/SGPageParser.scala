package home.sg.parser.html

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
   * @return a list of photo headers corresponding to the SG
   *  None, if the SG is a hopeful
   */
  def parseSetAlbumPageToSetHeaders(sgName: String, pageLines: List[String]): Option[List[PhotoSetHeader]] = {
    def isPreview(s: String) = s.contains("<div class=\"preview\"")
    def isPngSpank(s: String) = s.contains("<a class=\"pngSpank\"")
    def isDate(s: String) = s.contains("<div class=\"date\"")
    def isRelevant(s: String) = {
      val str = s.trim()
      isPreview(str) || isPngSpank(str) || isDate(str)
    }
    def isHopeful(pageLines: List[String]) = pageLines.exists(_.contains("alt=\"Hopeful Pics\""))

    if (isHopeful(pageLines))
      None
    else {
      val remaining = pageLines.filter(isRelevant).toList

      assume(remaining.length > 2, "we got a wrong page, there don't seem to be any relevant headers to album construction")
      assume(remaining.length % 3 == 0, "the number of lines filtered from the set album page was not a multiple of 3")
      assume(isPreview(remaining(0).trim), "the first string in a 3 tuple is not the preview")
      assume(isPngSpank(remaining(1).trim), "the second string in a 3 tuple is not the pngSpank")
      assume(isDate(remaining(2).trim), "the third string in a 3 tuple is not the date")

      val range = 3 to remaining.length by 3
      val htmlThreeTuples = for (n <- range) yield {
        val threeTupleAsList = remaining.slice(n - 3, n)
        (threeTupleAsList(0), threeTupleAsList(1), threeTupleAsList(2))
      }
      val headers = htmlThreeTuples map { threeTuple => PhotoSetHeader.build(sgName, threeTuple._1, threeTuple._2, threeTuple._3) }
      assume(headers.length > 0, "An albums page must have at least one album")
      Some(headers.toList.sortBy(_.relativeSaveLocation))
    }
  }

  /**
   * @param pageLines it take a set page in list form as parameter and returns
   * all the image URLs found on said page.
   * @return
   */
  def parseSetPageToImageURLs(pageLines: List[String]): List[String] = {
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
    val imageLines = pageLines.filter(_.contains("ImageHolder"))
    assume(imageLines.length > 0, "page does not contain any ImageHolders, this usually happens when you fetch the page without being logged in")
    val result = imageLines.toList map (s => replaceSpaces(s.substring(computeStartIndex(s), computeEndIndex(s))))
    assume(result.length > 0, "Image urls for could not be computed, this usually happens when you fetch the page without being logged in")
    result
  }
}