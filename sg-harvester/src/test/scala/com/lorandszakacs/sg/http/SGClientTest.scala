package com.lorandszakacs.sg.http

import com.lorandszakacs.util.time._
import com.lorandszakacs.sg._
import com.lorandszakacs.sg.http.impl.SGClientImpl
import com.lorandszakacs.util.effects._
import org.scalatest.flatspec.AnyFlatSpec

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 01 Jul 2017
  *
  */
class SGClientTest extends AnyFlatSpec with URLConversions {
  implicit lazy val sch: Scheduler = Scheduler.global

  private lazy val clientR = SGClientImpl()(HTTPIOScheduler(sch))

  behavior of "SG client"

  ignore should ".... get request with authentication token already available" in {
    val session = Session(
      username  = "",
      sessionID = "",
      csrfToken = "",
      expiresAt = Instant.unsafeNow(),
    )

    clientR.use { client =>
      Task {
        implicit val authentication: Authentication = client.createAuthentication(session).unsafeSyncGet()

        val html = client.getPage(s"${core.Domain}/girls/dwam/album/977051/limportance-d-etre-ernest/").unsafeSyncGet()
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

  }

}
