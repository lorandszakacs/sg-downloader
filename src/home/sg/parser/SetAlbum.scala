package home.sg.parser

class SetAlbum(val sgName: String, source: scala.io.Source) {

  private val parserResults = SGSetAlbumPageParser.parseSetAlbumPage(source)

  lazy val sets = parserResults map { threeTuple => new PhotoSetInfo(sgName, threeTuple._1, threeTuple._2, threeTuple._3) }

  lazy val pinkSets = sets.filterNot(_.isMR)

  lazy val mrSets = sets.filter(_.isMR)
}