package home.sg.parser

import home.sg.client.SGClient

object PhotoAlbumBuilder {

  /**
   * @param client; an already logged in client
   *
   * @return
   */
  def buildSetHeaders(sgName: String, client: SGClient): List[PhotoSetHeader] = {
    val albumPage = client.getSetAlbumPageSource(sgName)
    val setHeaders = SGPageParser.parseSetAlbumPageToSetHeaders(sgName, albumPage)
    setHeaders
  }

  def buildPhotoSet(sgName: String, header: PhotoSetHeader, client: SGClient, report: (Any => Unit)): PhotoSet = {
    report("fetching page info: %s".format(header.URL))
    val setPage = client.get(header.URL)
    val imageURLs = SGPageParser.parseSetPageToImageURLs(setPage)
    new PhotoSet(header, imageURLs)
  }

}