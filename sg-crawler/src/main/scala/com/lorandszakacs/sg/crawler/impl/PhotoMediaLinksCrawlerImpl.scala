package com.lorandszakacs.sg.crawler.impl

import com.lorandszakacs.sg.crawler.PhotoMediaLinksCrawler
import com.lorandszakacs.sg.http._
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{Future, ExecutionContext}

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 05 Jul 2016
  *
  */
private[crawler] class PhotoMediaLinksCrawlerImpl(private var sGClient: SGClient)(implicit val ec: ExecutionContext) extends PhotoMediaLinksCrawler with SGURLBuilder with StrictLogging {

  private[this] implicit var _authentication: Authentication = IdentityAuthentication

  override def authenticateIfNeeded(username: String, plainTextPassword: String): Future[Authentication] = {
    for {
      newAuthentication <- sGClient.authenticate(username, plainTextPassword)
    } yield {
      _authentication = newAuthentication
      newAuthentication
    }
  }

  override def gatherAllLinksForSetPage(photoSetPageUri: String): Future[List[String]] = {
    ???
  }

  override def authentication: Authentication = _authentication
}
