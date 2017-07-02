package com.lorandszakacs.sg.reifier

import java.net.URL

import com.lorandszakacs.sg.http.{Authentication, PasswordProvider}
import com.lorandszakacs.sg.model.{Hopeful, Photo, SuicideGirl}
import com.lorandszakacs.util.future._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 05 Jul 2016
  *
  */
trait SGReifier {

  /**
    * Warning, this is a stateful method!
    *
    * It will only be applied if [[authentication.needsRefresh]] is true.
    *
    * Its result is that a new [[Authentication]] is recreated where [[authentication.needsRefresh]]
    * will be false
    *
    */
  def authenticateIfNeeded()(implicit passwordProvider: PasswordProvider): Future[Authentication]

  @scala.deprecated("will be made private", "now")
  def gatherAllPhotosFromSetPage(photoSetPageUri: URL): Future[List[Photo]]

  def reifySuicideGirl(sg: SuicideGirl): Future[SuicideGirl]

  def reifyHopeful(hf: Hopeful): Future[Hopeful]

  def authentication: Authentication
}
