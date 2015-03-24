package com.lorandszakacs.sgd.daoservice.api

import spray.http.Uri
import com.github.nscala_time.time.Imports._

/**
 * @author Lorand Szakacs, lsz@lorandszakacs.com
 * @since 16 Mar 2015
 *
 */
case class SuicideGirl(
  uri: Uri,
  name: String,
  photoSets: List[PhotoSet]) {

  override lazy val toString =
    s"""
        ---------$name:${photoSets.length}---------
        uri=$uri
        ${photoSets.mkString("", "\n", "")}
      """.stripMargin(' ')
}

case class PhotoSet(
  uri: Uri,
  title: String,
  photos: List[Photo],
  date: DateTime) {

  override lazy val toString =
    s"""
        ${"\t"}title = $title
        ${"\t"}date  = ${date.toString(Util.dateTimeFormat)}
        ${"\t"}uri   = ${uri.toString()}
        ${"\t_________________"}
        ${photos.mkString("", "\t\t\n", "")}
        ${"\t================="}
      """.stripMargin(' ')
}

case class Photo(
  uri: Uri,
  index: Int) {

  override lazy val toString = s"\t\t${digitFormat(index)} -> $uri"

  private def digitFormat(n: Int) = if (n < 10) s"0$n" else "%2d".format(n)
}

private[api] object Util {
  final val dateTimeFormat = DateTimeFormat.forPattern("YYYY-MM-dd")
}