/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Lorand Szakacs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.lorandszakacs.sgd.app

import com.lorandszakacs.sgd.repl.Repl
import scala.io.StdIn
import com.lorandszakacs.sgd.http.Login
import com.lorandszakacs.sgd.http.SGClient
import akka.actor.ActorSystem
import scala.util.Success
import scala.util.Failure

object Main extends App {
  def shutdown(system: ActorSystem) {
    system.shutdown()
  }

  println("sg-downloader")
  println("please login to start.")
  val user = { print("user:"); StdIn.readLine() }
  val pwd = { print("pwd:"); val result = StdIn.readLine(); println(); result }
  implicit val system = ActorSystem("test-login-client")
  import system.dispatcher
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