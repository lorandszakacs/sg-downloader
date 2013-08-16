package home.sg.parser.html

object SGPageParser {

  /**
   * @param pageStream the function will close the stream
   * @return a list of photo headers corresponding to the SG
   *  None, if the SG is a hopeful
   */
  def parseSetAlbumPageToSetHeaders(name: String, pageLines: List[String]): List[PhotoSetHeader] = {
    def isHopeful(pageLines: List[String]) = pageLines.exists(_.contains("alt=\"Hopeful Pics\""))

    if (isHopeful(pageLines))
      parseHopefulAlbumPage(name, pageLines);
    else
      parseSGAlbumPage(name, pageLines)
  }

  /**
   * "<div class=\"preview\" id=\"album_277610\" title=\"Sash: The Grove\">",
   * "<a class=\"pngSpank\" href=\"/girls/Sash/albums/site/33360/\"><img src=\"/media/albums/0/36/33360/setpreview_medium.jpg\" width=\"169\" height=\"104\" alt=\"SuicideGirls: The Grove\" /></a>",
   * "<div class=\"date\">Apr 10, 2013</div>"
   */
  private def parseSGAlbumPage(sgName: String, pageLines: List[String]): List[PhotoSetHeader] = {
    def isPreview(s: String) = s.contains("<div class=\"preview\"")
    def isPngSpank(s: String) = s.contains("<a class=\"pngSpank\"")
    def isDate(s: String) = s.contains("<div class=\"date\"")
    def isRelevant(s: String) = {
      val str = s.trim()
      isPreview(str) || isPngSpank(str) || isDate(str)
    }
    val remaining = pageLines.filter(isRelevant).toList

    assume(remaining.length > 2, "SGAlbumPageParsing: we got a wrong page, there don't seem to be any relevant headers to album construction")
    assume(remaining.length % 3 == 0, "SGAlbumPageParsing: the number of lines filtered from the set album page was not a multiple of 3")
    assume(isPreview(remaining(0).trim), "SGAlbumPageParsing: the first string in a 3 tuple is not the preview")
    assume(isPngSpank(remaining(1).trim), "SGAlbumPageParsing: the second string in a 3 tuple is not the pngSpank")
    assume(isDate(remaining(2).trim), "SGAlbumPageParsing: the third string in a 3 tuple is not the date")

    val range = 3 to remaining.length by 3
    val htmlThreeTuples = for (n <- range) yield {
      val threeTupleAsList = remaining.slice(n - 3, n)
      (threeTupleAsList(0), threeTupleAsList(1), threeTupleAsList(2))
    }
    val headers = htmlThreeTuples map { threeTuple => PhotoSetHeader.build(sgName, threeTuple._1, threeTuple._2, threeTuple._3) }
    assume(headers.length > 0, "SGAlbumPageParsing: An albums page must have at least one album")
    headers.toList.sortBy(_.relativeSaveLocation).reverse
  }

  /**
   * For hopefuls this is the only part of the page that we care about:
   *
   * 			<img  src="/img/member/heading_hopeful_pics.gif" width="111" height="30" alt="Hopeful Pics" align="top" />
   * </div>
   * </div>
   * <div class="browserContent">
   * <div class="card long">
   * <h1><a href="/members/Dalmasca/albums/site/33209/">Picker-Uppers</a></h1>
   * <a href="/members/Dalmasca/albums/site/33209/">
   * <img src="http://img.suicidegirls.com/media/albums/9/20/33209/previewmedium.jpg?1364025600" width="167" height="167" alt="Picker-Uppers" />
   * </a>
   * <div class="info">
   * <p>
   * <span class="prefix">PICS:</span> 49				</p>
   * <p>
   * <span class="prefix" title="This set is in the Member Review section. Members can see and comment on it.">UP SINCE:</span> Mar 23 2013				</p>
   * <p>
   * <span class="prefix">COMMENTS:</span>
   * <a href="/boards/The+Pictures/389624/">964</a>
   * </p>
   *
   * </div>
   * </div>
   * <div class="card long">
   * <h1><a href="/members/Dalmasca/albums/site/21968/">What a Travesty</a></h1>
   * <a href="/members/Dalmasca/albums/site/21968/">
   * <img src="http://img.suicidegirls.com/media/albums/8/96/21968/previewmedium.jpg?1291413600" width="167" height="167" alt="What a Travesty" />
   * </a>
   * <div class="info">
   * <p>
   * <span class="prefix">PICS:</span> 60				</p>
   * <p>
   * <span class="prefix" title="This set is in the Member Review section. Members can see and comment on it.">UP SINCE:</span> Dec 3 2010				</p>
   * <p>
   * <span class="prefix">COMMENTS:</span>
   * <a href="/boards/The+Pictures/343828/">1081</a>
   * </p>
   *
   * </div>
   * </div>
   * <div class="card long">
   * <h1><a href="/members/Dalmasca/albums/site/21583/">Marauding Ghouls</a></h1>
   * <a href="/members/Dalmasca/albums/site/21583/">
   * <img src="http://img.suicidegirls.com/media/albums/3/58/21583/previewmedium.jpg?1288717200" width="167" height="167" alt="Marauding Ghouls" />
   * </a>
   * <div class="info">
   * <p>
   * <span class="prefix">PICS:</span> 54				</p>
   * <p>
   * <span class="prefix" title="This set is in the Member Review section. Members can see and comment on it.">UP SINCE:</span> Nov 2 2010				</p>
   * <p>
   * <span class="prefix">COMMENTS:</span>
   * <a href="/boards/The+Pictures/342006/">581</a>
   * </p>
   *
   * </div>
   * </div>
   * </div>
   * </div>
   * </div>
   * <div id="pics">
   * <div class="header hd1">
   * <img src="http://img.suicidegirls.com/img/member/heading_albums.gif" width="81" height="28" alt="ALBUMS" />
   */
  private def parseHopefulAlbumPage(hopefulName: String, pageLines: List[String]): List[PhotoSetHeader] = {
    def extractMiddleOfList[T](list: List[T], startPredicate: (T => Boolean), endPredicate: (T => Boolean)) =
      list.dropWhile(startPredicate).tail.takeWhile(endPredicate)

    //this line contains the link and the name of the set
    //<h1><a href="/members/Dalmasca/albums/site/21968/">What a Travesty</a></h1>
    def isHeaderContainingNameAndLink(line: String): Boolean =
      line.trim.startsWith("<h1><a href=")

    //<span class="prefix" title="This set is in the Member Review section. Members can see and comment on it.">UP SINCE:</span> Dec 3 2010</p>
    def isLineContainingUpSince(line: String): Boolean =
      line.contains("UP SINCE:")

    def isRelevant(line: String) = isHeaderContainingNameAndLink(line) || isLineContainingUpSince(line)

    val startDelimiter = "\"Hopeful Pics\""
    val endDelimiter = "\"ALBUMS\""
    val onlySets = extractMiddleOfList[String](pageLines, !_.contains(startDelimiter), !_.contains(endDelimiter))
    val remaining = onlySets.filter(isRelevant)

    assume(remaining.length > 1, "HopefulAlbumPageParsing: we got a wrong page, there don't seem to be any relevant headers to album construction")
    assume(remaining.length % 2 == 0, "HopefulAlbumPageParsing: the number of lines filtered from the set album page was not a multiple of 3")
    assume(isHeaderContainingNameAndLink(remaining(0).trim), "HopefulAlbumPageParsing: the first string in a 2 tuple is not the header")
    assume(isLineContainingUpSince(remaining(1).trim), "HopefulAlbumPageParsing: the second string in a 2 tuple is not the UP SINCE")

    val range = 2 to remaining.length by 2
    val htmlTwoTuples = for (n <- range) yield {
      val twoTupleAsList = remaining.slice(n - 2, n)
      (twoTupleAsList(0).trim, twoTupleAsList(1).trim)
    }
    val headers = htmlTwoTuples map { twoTuple => PhotoSetHeader.build(hopefulName, twoTuple._1, twoTuple._2) }
    assume(headers.length > 0, "HopefulAlbumPageParsing: An albums page must have at least one album")
    headers.toList.sortBy(_.relativeSaveLocation).reverse
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