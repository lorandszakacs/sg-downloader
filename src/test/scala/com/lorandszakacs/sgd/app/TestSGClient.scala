package com.lorandszakacs.sgd.app

import scala.io.StdIn
import scala.language.postfixOps
import scala.util.{ Failure, Success }
import com.lorandszakacs.sgd.http.SGClient
import akka.actor.ActorSystem
import spray.http.Uri
import scala.concurrent.Await
import scala.concurrent.duration._

object TestSGClient extends App {
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

  val sashPhotoSetsPage = Uri("https://suicidegirls.com/girls/sash/photos/view/photosets/")

  def sashAlbums = {
    Await.result(sgClient.getPhotoSetUris(sashPhotoSetsPage), 1 minute).get
  }

  println(sashAlbums.mkString("\n"))

  system.shutdown()
}