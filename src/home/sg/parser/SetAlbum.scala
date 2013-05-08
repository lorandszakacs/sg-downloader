package home.sg.parser

class SetAlbum(val sgName: String, source: scala.io.Source) {

  private val parserResults = SGSetAlbumPageParser.parseSetAlbumPage(source)

  def sets = parserResults map { threeTuple => new PhotoSetInfo(sgName, threeTuple._1, threeTuple._2, threeTuple._3) }

  def pinkSets = sets.filterNot(_.isMR)

  def mrSets = sets.filter(_.isMR)
}