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

import java.time.LocalDate

import spray.http.Uri

/**
 * @author Lorand Szakacs, lsz@lorandszakacs.com
 * @since 16 Mar 2015
 *
 */
object SuicideGirl {
  def apply(uri: => Uri, name: => String, photoSets: => List[PhotoSet]) =
    new SuicideGirl(uri, name, photoSets)
}

class SuicideGirl private(
  uriP: => Uri,
  nameP: => String,
  photoSetsP: => List[PhotoSet]) {

  lazy val path = nameP
  lazy val uri = uriP
  lazy val name = nameP
  lazy val photoSets = photoSetsP

  override lazy val toString =
    s"""
---------${name}:${photoSets.length}---------
uri=${uri}
${photoSets.mkString("", "\n", "")}
"""
}

object PhotoSet {
  def apply(uri: => Uri, title: => String, photos: => List[Photo], date: => LocalDate, ownerSuicideGirl: => SuicideGirl) =
    new PhotoSet(uri, title, photos, date, ownerSuicideGirl)
}

class PhotoSet private(
  uriP: => Uri,
  titleP: => String,
  photosP: => List[Photo],
  dateP: => LocalDate,
  ownerSuicideGirlP: => SuicideGirl) {

  lazy val uri: Uri = uriP
  lazy val title: String = titleP
  lazy val photos: List[Photo] = photosP
  lazy val date: LocalDate = dateP
  lazy val ownerSuicideGirl: SuicideGirl = ownerSuicideGirlP

  private def digitFormat(n: Int) = if (n < 10) s"0$n" else "%2d".format(n)

  private def readableDate = s"${date.getYear()}-${digitFormat(date.getMonthValue)}-${digitFormat(date.getDayOfMonth())}"

  def normalizedName = title.replace(" ", "-").replace("?", "-").replace("/", "-").replace("\\", "-")

  def path = s"${ownerSuicideGirl.path}/${readableDate}-${normalizedName}"

  override lazy val toString =
    s"""
${"\t"}title = ${title}
${"\t"}date  = ${date.toString}
${"\t"}uri   = ${uri.toString}
${"\t"}path  = ${path}
${"\t_________________"}
${photos.mkString("", "\t\t\n", "")}
${"\t================="}"""
}

object Photo {
  def apply(uri: => Uri, index: => Int, containingPhotoSet: => PhotoSet) =
    new Photo(uri, index, containingPhotoSet)
}

class Photo private(
  uriP: => Uri,
  indexP: => Int,
  containingPhotoSetP: => PhotoSet) {

  lazy val uri: Uri = uriP
  lazy val index: Int = indexP
  lazy val containingPhotoSet: PhotoSet = containingPhotoSetP

  def path = s"${containingPhotoSet.path}/${containingPhotoSet.path.replace("/", ".")}-${index}"

  private def digitFormat(n: Int) = if (n < 10) s"0$n" else "%2d".format(n)

  override lazy val toString = s"\t\t${digitFormat(index)} -> ${uri}"
}

case class SuicideGirlShallow(
  uri: Uri,
  name: String,
  photoSets: List[PhotoSetShallow]) {
  def apply(): SuicideGirl = {
    def sg: SuicideGirl = SuicideGirl(uri, name, photoSets.map(_.apply(sg)))
    sg
  }

  override lazy val toString =
    s"""
---------${name}:${photoSets.length}---------
uri=${uri}
${photoSets.mkString("", "\n", "")}
"""
}

case class PhotoSetShallow(
  uri: Uri,
  title: String,
  photos: List[PhotoShallow],
  date: LocalDate) {
  def apply(sg: SuicideGirl): PhotoSet = {
    def set: PhotoSet = PhotoSet(uri, title, photos.map(_.apply(set)), date, sg)
    set
  }

  override lazy val toString =
    s"""
${"\t"}title = ${title}
${"\t"}date  = ${date.toString}
${"\t"}uri   = ${uri.toString}
${"\t_________________"}
${photos.mkString("", "\t\t\n", "")}
${"\t================="}"""
}

case class PhotoShallow(
  uri: Uri,
  index: Int) {
  def apply(photoSet: PhotoSet): Photo = {
    def photo: Photo = Photo(uri, index, photoSet)
    photo
  }

  private def digitFormat(n: Int) = if (n < 10) s"0$n" else "%2d".format(n)

  override lazy val toString = s"\t\t${digitFormat(index)} -> ${uri}"
}