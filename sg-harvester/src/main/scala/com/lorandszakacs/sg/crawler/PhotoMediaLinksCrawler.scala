package com.lorandszakacs.sg.crawler

import java.net.URL

import com.lorandszakacs.sg.http.{Authentication, PasswordProvider}
import com.lorandszakacs.sg.model.Photo
import com.lorandszakacs.util.future._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 05 Jul 2016
  *
  */
trait PhotoMediaLinksCrawler {

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

  def gatherAllPhotosFromSetPage(photoSetPageUri: URL): Future[List[Photo]]

  def authentication: Authentication
}
