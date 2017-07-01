package com.lorandszakacs.sg.reifier

import com.lorandszakacs.sg.http.Session
import com.lorandszakacs.util.future._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 20 Jul 2016
  *
  */
private[reifier] trait SessionDao {

  def create(session: Session): Future[Unit]

  def find(): Future[Option[Session]]

}