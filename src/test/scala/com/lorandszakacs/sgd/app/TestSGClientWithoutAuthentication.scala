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

import java.io.File

import akka.actor.ActorSystem
import com.lorandszakacs.sgd.http.SGClient

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * @author Lorand Szakacs, lsz@lorandszakacs.com
 * @since 16 Mar 2015
 *
 */
object TestSGClientWithoutAuthentication extends App {
  implicit val system = ActorSystem("test-login-client")

  import com.lorandszakacs.sgd.app.TestSGClientWithoutAuthentication.system.dispatcher

  val sgClient = SGClient()
  //  val allSGNames = SGNames(Int.MaxValue)
  val allHopefuls = HopefulNames(Int.MaxValue)

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

  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(f)
    try {
      op(p)
    } finally {
      p.close()
    }
  }

  //  albums(allSGNames.head)
  albums(allHopefuls.head)

  system.shutdown()
}