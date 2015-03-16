/**
 * Copyright 2015 Lorand Szakacs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.lorandszakacs.sgd.model

import com.github.nscala_time.time.Imports._
import spray.http.Uri

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

private[model] object Util {
  final val dateTimeFormat = DateTimeFormat.forPattern("YYYY-MM-dd")
}