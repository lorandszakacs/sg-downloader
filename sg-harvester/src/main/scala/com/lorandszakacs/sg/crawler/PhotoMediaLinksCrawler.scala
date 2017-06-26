package com.lorandszakacs.sg.crawler

import java.net.URL

import com.lorandszakacs.sg.http.Authentication
import com.lorandszakacs.sg.model.Photo

import scala.concurrent.Future

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
  def authenticateIfNeeded(usernameAndPassword: () => (String, String)): Future[Authentication]

  def gatherAllPhotosFromSetPage(photoSetPageUri: URL): Future[List[Photo]]

  def authentication: Authentication
}
