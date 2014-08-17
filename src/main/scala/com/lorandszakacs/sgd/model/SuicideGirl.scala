package com.lorandszakacs.sgd.model

import spray.http.Uri
import java.time.LocalDate

case class SuicideGirl(
  val uri: Uri,
  val name: String) {

  val path = name
}

case class PhotoSet(
  val uri: Uri,
  val name: String,
  val photos: List[Photo],
  val date: LocalDate,
  val ownerSuicideGirl: SuicideGirl) {

  private val readableDate = s"${date.getYear()}.${date.getMonth()}.${date.getDayOfMonth()}"

  val normalizedName = name.replace(" ", "-").replace("?", "-").replace("/", "-").replace("\\", "-")
  val path = s"${ownerSuicideGirl.path}/${readableDate}-${normalizedName}"
}

case class Photo(
  val uri: Uri,
  val index: Int,
  val containingPhotoSet: PhotoSet) {

  val path = s"${containingPhotoSet.path}/${containingPhotoSet.path.replace("/", ".")}-${index}"
}