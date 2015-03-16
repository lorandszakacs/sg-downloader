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
import com.lorandszakacs.sgd.http.Login

import scala.io.StdIn
import scala.util.{Failure, Success}

/**
 * @author Lorand Szakacs, lsz@lorandszakacs.com
 * @since 16 Mar 2015
 *
 */
object TestLogin extends App {
  implicit val system = ActorSystem("test-login-client")
  import com.lorandszakacs.sgd.app.TestLogin.system.dispatcher

  val Referer = "https://suicidegirls.com/"
  val initialAccessPoint = "https://suicidegirls.com"
  val loginAccessPoint = "https://suicidegirls.com/login/"
  val user = { print("user:"); StdIn.readLine() }
  val pwd = { print("pwd:"); val result = StdIn.readLine(); println(); result }
  val loginInfo = Login.apply(initialAccessPoint, loginAccessPoint, Referer, user, pwd) match {
    case Success(info) =>
      println(info.toString)
      info
    case Failure(e) =>
      println(e.toString())
      throw e
  }

}