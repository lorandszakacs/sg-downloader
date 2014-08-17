package com.lorandszakacs.sgd.model

import spray.http.Uri
import java.time.LocalDate

case class SuicideGirl(
  val uri: Uri,
  val name: String)

case class PhotoSet(
  val uri: Uri,
  val name: String,
  val photos: List[Photo],
  val containingSuicideGirl: SuicideGirl,
  val date: LocalDate)

case class Photo(
  val uri: Uri,
  val index: String,
  val containingPhotoSet: PhotoSet)