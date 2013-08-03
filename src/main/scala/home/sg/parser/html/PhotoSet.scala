package home.sg.parser.html

import home.sg.util.IO


class PhotoSet(header: PhotoSetHeader, val imageURLs: List[String]) {

  val sgName: String = header.sgName
  val title: String = header.title

  val relativeSaveLocation: String = header.relativeSaveLocation

  val URLSaveLocationPairs: List[(String, String)] = imageURLs.zip((1 to imageURLs.length).map(imageSaveLocation))

  override def toString: String = {
    val result = URLSaveLocationPairs.map(p => "  " + p._2 + "     =>     " + p._1)
    result.mkString("\n")
  }

  private def imageSaveLocation(i: Int) = IO.concatPath(relativeSaveLocation, "%02d.jpg".format(i))
}