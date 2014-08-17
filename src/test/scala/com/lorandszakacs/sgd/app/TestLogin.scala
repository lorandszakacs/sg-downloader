package com.lorandszakacs.sgd.app

import scala.util.{ Success, Failure }
import scala.concurrent.duration._
import akka.actor.ActorSystem
import spray.client.pipelining._
import spray.util._
import com.lorandszakacs.util.html._

object TestLogin extends App {
  //the login page contains something like this:
  // <form id="login-form" method="post" action="/login/"><input type='hidden' name='csrfmiddlewaretoken' value='RShI5GUhjY5OWlcK3G2UdCCs7QdI1Ukm' />

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("simple-spray-client")
  import system.dispatcher // execution context for futures below

  val pipeline = sendReceive

  val responseFuture = pipeline {
    Get("https://suicidegirls.com")
  }
  responseFuture onComplete {
    case Success(result) =>
      println(result.headers.mkString("\n"))
      println("=============================================")
      //      println(result.entity.asString)
      val html = HtmlProcessor(result.entity.asString)
      val csrdmiddlewaretoken = html filter Tag("form") && Tag("input") && Value(Attribute("value"))
      println(s"\nMIDDLEWARETOKEN: ${csrdmiddlewaretoken}\n")
      println("=============================================")
      shutdown()

    case Failure(error) =>
      println(s"ERROR!!!, ${error.getMessage()}")
      shutdown()
  }

  def shutdown(): Unit = {
    system.shutdown()
  }
}