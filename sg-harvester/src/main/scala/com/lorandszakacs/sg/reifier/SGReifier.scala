package com.lorandszakacs.sg.reifier

import java.net.URL

import com.lorandszakacs.sg.http.{Authentication, PasswordProvider, PatienceConfig}
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

  def reifySuicideGirl(sg: SuicideGirl)(implicit pc: PatienceConfig): Future[SuicideGirl]

  def reifyHopeful(hf: Hopeful)(implicit pc: PatienceConfig): Future[Hopeful]

  def authentication: Authentication
}
