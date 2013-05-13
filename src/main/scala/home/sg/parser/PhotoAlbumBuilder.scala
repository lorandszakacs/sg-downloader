package home.sg.parser

import home.sg.client.SGClient

object PhotoAlbumBuilder {

  /**
   * @param client; an already logged in client
   *
   * @return
   */
  def buildSets(sgName: String, client: SGClient, report: (Any => Unit)): List[PhotoSet] = {
    val setHeaders = computeSetHeaders(sgName, client)

    buildPhotoSets(sgName, setHeaders, client, report)
  }

  def computeSetHeaders(sgName: String, client: SGClient): List[PhotoSetHeader] = {
    val albumPage = client.getSetAlbumPageSource(sgName)
    val setHeaders = SGPageParser.parseSetAlbumPageToSetHeaders(sgName, albumPage)
    setHeaders
  }

  def buildPhotoSets(sgName: String, headers: List[PhotoSetHeader], client: SGClient, report: (Any => Unit)): List[PhotoSet] = {
    val photoSetPages = headers.map(h => {
      report("fetching page info: %s".format(h.URL))
      client.get(h.URL)
    })

    val photoSets = (headers zip photoSetPages) map { p =>
      val header = p._1
      val page = p._2
      val imageURLs = SGPageParser.parseSetPageToImageURLs(page)
      new PhotoSet(header, imageURLs)
    }

    photoSets
  }

}