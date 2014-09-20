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

import scala.io.StdIn
import scala.language.postfixOps
import scala.util.{ Failure, Success }
import com.lorandszakacs.sgd.http.SGClient
import akka.actor.ActorSystem
import spray.http.Uri
import scala.concurrent.Await
import scala.concurrent.duration._
import java.io.File
import com.lorandszakacs.sgd.http.SGClient

object TestSGClientWithoutAuthentication extends App {
  implicit val system = ActorSystem("test-login-client")
  import system.dispatcher

  val sgClient = SGClient()

  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(f)
    try { op(p) } finally { p.close() }
  }

  def albums(name: String) = {
    val result = Await.result(sgClient.getPhotoSetUris(name), 1 minute).get
    println(s"------ ${name}:${result.size}---------")
    println(result.mkString("\n"))
    println("---------------------------------------")
    result
  }

  def SGNames(limit: Int): Seq[String] = {
    def reporter = new SGClient.Reporter {
      def apply(offset: Int, offsetStep: Int) = {
        if (offset % (offsetStep * 6) == 0)
          println(s"at:$offset")
      }
    }

    val result = Await.result(sgClient.gatherSGNames(limit, reporter), 1 hour).get.sorted
    println(s"-----SGNAMES: ${result.length}")
    val file = new File("/Users/lorand/Downloads/sgs/sgs.txt")
    if (!file.exists()) {
      printToFile(file)(p => {
        result.foreach(p.println)
      })
    }
    result
  }

  def HopefulNames(limit: Int): Seq[String] = {
    def reporter = new SGClient.Reporter {
      def apply(offset: Int, offsetStep: Int) = {
        if (offset % (offsetStep * 5) == 0)
          println(s"at:$offset")
      }
    }

    val result = Await.result(sgClient.gatherHopefulNames(limit, reporter), 1 hour).get.distinct.sorted
    println(s"-----Hopefuls: ${result.length}")
    val file = new File("/Users/lorand/Downloads/sgs/hopefuls.txt")
    if (!file.exists()) {
      printToFile(file)(p => {
        result.foreach(p.println)
      })
    }
    result
  }

  //  val allSGNames = SGNames(Int.MaxValue)
  val allHopefuls = HopefulNames(Int.MaxValue)

  //  albums(allSGNames.head)
  albums(allHopefuls.head)

  system.shutdown()
}