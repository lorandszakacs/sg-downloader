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

  testGetPage

  def testGetPage {
    val URI = """https://suicidegirls.com/girls/dwam/album/977051/limportance-d-etre-ernest/"""
    val html = Await.result(sgClient.getPage(URI), 1 minute)
    html filter Class("image-section") && Tag("li") && Class("photo-container") && RetainFirst(HrefLink()) match {
      case Some(links) => println(links.mkString("\n"))
      case None => s"FAILED TO GRAB ALL LINKS! here is the page that was returned:${html.document.toString}"
    }

  }

}