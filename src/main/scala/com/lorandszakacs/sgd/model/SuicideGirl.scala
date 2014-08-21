package com.lorandszakacs.sgd.model

import spray.http.Uri
import java.time.LocalDate

class SuicideGirl(
  uriP: => Uri,
  nameP: => String,
  photoSetsP: => List[PhotoSet]) {

  lazy val path = nameP
  lazy val uri = uriP
  lazy val name = nameP
  lazy val photoSets = photoSetsP
}

class PhotoSet(
  uriP: => Uri,
  nameP: => String,
  photosP: => List[Photo],
  dateP: => LocalDate,
  ownerSuicideGirlP: => SuicideGirl) {

  lazy val uri: Uri = uriP
  lazy val name: String = nameP
  lazy val photos: List[Photo] = photosP
  lazy val date: LocalDate = dateP
  lazy val ownerSuicideGirl: SuicideGirl = ownerSuicideGirlP

  private def readableDate = s"${date.getYear()}.${date.getMonth()}.${date.getDayOfMonth()}"

  def normalizedName = name.replace(" ", "-").replace("?", "-").replace("/", "-").replace("\\", "-")
  def path = s"${ownerSuicideGirl.path}/${readableDate}-${normalizedName}"
}

class Photo(
  uriP: => Uri,
  indexP: => Int,
  containingPhotoSetP: => PhotoSet) {

  lazy val uri: Uri = uriP
  lazy val index: Int = indexP
  lazy val containingPhotoSet: PhotoSet = containingPhotoSetP

  def path = s"${containingPhotoSet.path}/${containingPhotoSet.path.replace("/", ".")}-${index}"
}