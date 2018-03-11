package com.lorandszakacs.sg.reifier

import com.lorandszakacs.sg.http.{Authentication, PatienceConfig}
import com.lorandszakacs.sg.model.{HF, M, SG}
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
  def authenticateIfNeeded(): IO[Authentication]

  def reifySG(sg: SG)(implicit pc: PatienceConfig): IO[SG]

  def reifyHF(hf: HF)(implicit pc: PatienceConfig): IO[HF]

  def reifyM(m: M)(implicit pc: PatienceConfig): IO[M]

  def authentication: Authentication
}
