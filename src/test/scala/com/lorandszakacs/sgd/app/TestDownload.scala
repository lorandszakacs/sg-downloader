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

  lazy val links = testGetPage
  val URI = """https://suicidegirls.com/girls/dwam/album/977051/limportance-d-etre-ernest/"""

  def testGetPage: List[Uri] = {
    val html = Await.result(sgClient.getPage(URI), 1 minute)
    html filter Class("image-section") && Tag("li") && Class("photo-container") && RetainFirst(HrefLink()) match {
      case Some(links) =>
        //        println(links.mkString("\n"))
        links.map(Uri(_))
      case None => throw new Exception(s"FAILED TO GRAB ALL LINKS! here is the page that was returned:${html.document.toString}")
    }
  }

  def dwam: SuicideGirl = new SuicideGirl(Uri("https://suicidegirls.com/girls/dwam/"), "Dwam", List(limportance))
  def limportance: PhotoSet = new PhotoSet(URI, "Limportance-d-etre-ernest", photos, LocalDate.now(), dwam)
  def photos: List[Photo] = links.zip(1 to links.length).map(pair => new Photo(pair._1, pair._2, limportance))

  println(dwam.photoSets.head.photos.map(_.uri).mkString("\n"))
}