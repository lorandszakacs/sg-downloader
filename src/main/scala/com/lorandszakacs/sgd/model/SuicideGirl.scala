package com.lorandszakacs.sgd.model

import java.time.LocalDate

import spray.http.Uri

object SuicideGirl {
  def apply(uri: => Uri, name: => String, photoSets: => List[PhotoSet]) =
    new SuicideGirl(uri, name, photoSets)
}

class SuicideGirl private (
  uriP: => Uri,
  nameP: => String,
  photoSetsP: => List[PhotoSet]) {

  lazy val path = nameP
  lazy val uri = uriP
  lazy val name = nameP
  lazy val photoSets = photoSetsP
}

object PhotoSet {
  def apply(uri: => Uri, title: => String, photos: => List[Photo], date: => LocalDate, ownerSuicideGirl: => SuicideGirl) =
    new PhotoSet(uri, title, photos, date, ownerSuicideGirl)
}

class PhotoSet private (
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

  private def readableDate = s"${date.getYear()}.${date.getMonth()}.${date.getDayOfMonth()}"

  def normalizedName = title.replace(" ", "-").replace("?", "-").replace("/", "-").replace("\\", "-")
  def path = s"${ownerSuicideGirl.path}/${readableDate}-${normalizedName}"
}

object Photo {
  def apply(uri: => Uri, index: => Int, containingPhotoSet: => PhotoSet) =
    new Photo(uri, index, containingPhotoSet)
}

class Photo private (
  uriP: => Uri,
  indexP: => Int,
  containingPhotoSetP: => PhotoSet) {

  lazy val uri: Uri = uriP
  lazy val index: Int = indexP
  lazy val containingPhotoSet: PhotoSet = containingPhotoSetP

  def path = s"${containingPhotoSet.path}/${containingPhotoSet.path.replace("/", ".")}-${index}"
}

case class SuicideGirlShallow(
  uri: Uri,
  name: String,
  photoSets: List[PhotoSetShallow]) {
  def apply(): SuicideGirl = {
    def sg: SuicideGirl = SuicideGirl(uri, name, photoSets.map(_.apply(sg)))
    sg
  }

  override def toString =
    s"""
name=${name}
uri=${uri}
photosets=${photoSets.length}
+++
${photoSets.mkString("---")}
+++
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

  override def toString =
    s"""
uri=${uri}
title=${title}
date=${date.toString()}
photos=
  ${photos.mkString("\n\t")}
"""
}

case class PhotoShallow(
  uri: Uri,
  index: Int) {
  override def toString = s"$index -> $uri"
  def apply(photoSet: PhotoSet): Photo = {
    def photo: Photo = Photo(uri, index, photoSet)
    photo
  }
}