/**
 * Copyright 2015 Lorand Szakacs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.lorandszakacs.sgd.app

import akka.actor.ActorSystem
import com.lorandszakacs.sgd.http.SGClient
import com.lorandszakacs.sgd.repl.Repl

import scala.io.StdIn
import scala.util.{Failure, Success}

/**
 * @author Lorand Szakacs, lsz@lorandszakacs.com
 * @since 16 Mar 2015
 *
 */
object Main extends App {
  val user = {
    print("user:");
    StdIn.readLine()
  }

  println("sg-downloader")
  println("please login to start.")
  val pwd = {
    print("pwd:");
    val result = StdIn.readLine();
    println();
    result
  }
  implicit val system = ActorSystem("test-login-client")

  def shutdown(system: ActorSystem) {
    system.shutdown()
  }

  import com.lorandszakacs.sgd.app.Main.system.dispatcher

  SGClient(user, pwd) match {
    case Success(sgClient) =>
      println(sgClient.authentication.toString)
      val repl = new Repl()
      repl.start()
      println("exiting sg-downloader.")
      shutdown(system)
    case Failure(e) =>
      System.err.println(s"failed to login:\n${e.getMessage}")
      println(s"exiting sg-downloader")
      shutdown(system)
  }
}