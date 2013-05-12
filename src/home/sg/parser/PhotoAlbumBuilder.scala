package home.sg.parser

import home.sg.client.SGClient

object PhotoAlbumBuilder {

  /**
   * @param client; an already logged in client
   *
   * @return
   */
  def buildSets(sgName: String, client: SGClient): List[PhotoSet] = {
    buildSets(sgName, client, { (x: Any) => Unit })
  }

  def buildSets(sgName: String, client: SGClient, report: (Any => Unit)): List[PhotoSet] = {
    val albumPage = client.getSetAlbumPageSource(sgName)
    val setHeaders = SGPageParser.parseSetAlbumPageToSetHeaders(sgName, albumPage)
    setHeaders foreach println
    val photoSets = setHeaders map createPhotoSet(client, report)
    photoSets
  }

  private def createPhotoSet(client: SGClient, report: (Any => Unit))(header: PhotoSetHeader) =
    new PhotoSet(header, computeImageURLs(client, report)(header))

  private def computeImageURLs(client: SGClient, report: (Any => Unit))(header: PhotoSetHeader) = {
    report("fetching set page: %s".format(header.URL))
    val setPage = client.get(header.URL)
    val imageURLs = SGPageParser.parseSetPageToImageURLs(setPage)
    imageURLs
  }
}