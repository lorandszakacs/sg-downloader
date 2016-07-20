package com.lorandszakacs.sg.crawler

import com.lorandszakacs.sg.http.Session
import com.lorandszakacs.util.monads.future.FutureUtil.Future

/**
  *
  * @author Lorand Szakacs, lorand.szakacs@busymachines.com, lsz@lorandszakacs.com
  * @since 20 Jul 2016
  *
  */
private[crawler] trait SessionDao {

  def create(session: Session): Future[Unit]

  def find(): Future[Option[Session]]

}