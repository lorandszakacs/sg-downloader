package com.lorandszakacs.sgd.app

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.io.StdIn
import scala.language.postfixOps
import scala.util.{ Failure, Success }
import com.lorandszakacs.commons.html._
import com.lorandszakacs.sgd.http.SGClient
import akka.actor.ActorSystem
import spray.http.Uri.apply
import spray.http.Uri
import java.time.LocalDate
import com.lorandszakacs.sgd.model._
import com.lorandszakacs.sgd.client.data.PhotoSetPage
import com.lorandszakacs.sgd.http.Parser

object TestDownload extends App {
  implicit val system = ActorSystem("test-login-client")
  import system.dispatcher

  val user = { print("user:"); StdIn.readLine() }
  val pwd = { print("pwd:"); val result = StdIn.readLine(); println(); result }
  val sgClient = SGClient(user, pwd) match {
    case Success(client) =>
      println(client.authentication.toString)
      client
    case Failure(e) =>
      throw e
  }

  system.shutdown()
}