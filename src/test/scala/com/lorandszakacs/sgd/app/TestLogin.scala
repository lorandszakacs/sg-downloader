package com.lorandszakacs.sgd.app

import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import akka.actor.ActorSystem
import spray.client.pipelining._
import spray.http.FormData
import spray.http.HttpCookie
import spray.http.HttpHeader
import spray.http.HttpHeaders.Cookie
import spray.http.HttpHeaders.RawHeader
import spray.http.HttpRequest
import spray.http.HttpResponse
import spray.http.StatusCodes
import scala.io.StdIn
import scala.io.Source
import com.lorandszakacs.sgd.http.Login

object TestLogin extends App {
  implicit val system = ActorSystem("test-login-client")
  import system.dispatcher

  val Referer = "https://suicidegirls.com/"
  val initialAccessPoint = "https://suicidegirls.com"
  val loginAccessPoint = "https://suicidegirls.com/login/"
  val user = { print("user:"); StdIn.readLine() }
  val pwd = { print("\npwd:"); val result = StdIn.readLine(); println(); result }
  val loginInfo = Login.apply(initialAccessPoint, loginAccessPoint, Referer, user, pwd) match {
    case Success(info) =>
      println(info.toString)
      info
    case Failure(e) =>
      println(e.toString())
      throw e
  }

}