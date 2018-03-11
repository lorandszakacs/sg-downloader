package com.lorandszakacs.sg.http

import akka.actor.ActorSystem
import com.github.nscala_time.time.Imports._
import com.lorandszakacs.sg._
import com.lorandszakacs.sg.http.impl.SGClientImpl
import com.lorandszakacs.util.effects._
import org.scalatest.FlatSpec

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 01 Jul 2017
  *
  */
class SGClientTest extends FlatSpec with URLConversions {

  implicit lazy val as: ActorSystem = ActorSystem("test-actor-system")
  implicit lazy val sch: Scheduler   = Scheduler.global

  lazy val client = SGClientImpl()

  behavior of "SG client"

  ignore should ".... get request with authentication token already available" in {
    val session = Session(
      username  = "",
      sessionID = "",
      csrfToken = "",
      expiresAt = DateTime.now(DateTimeZone.forID("GMT"))
    )

    implicit val authentication: Authentication = client.createAuthentication(session).unsafeSyncGet()

    val html = client.getPage(s"${core.Domain}/members/odina/album/2745718/do-i-wanna-know/").unsafeSyncGet()
    println {
      s"""
         |
         |
         |${html.toString}
         |
         |
      """.stripMargin
    }
  }

}
