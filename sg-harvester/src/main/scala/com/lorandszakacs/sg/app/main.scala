/**
  * Copyright 2016 Lorand Szakacs
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
package com.lorandszakacs.sg.app

import java.util.concurrent.Executors

import akka.actor.ActorSystem
import com.lorandszakacs.sg.app.repl.HarvesterRepl
import com.lorandszakacs.sg.harvester.SGHarvesterAssembly
import com.typesafe.scalalogging.StrictLogging
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.language.postfixOps

/**
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 16 Mar 2015
  *
  */
object Main extends App with StrictLogging {

  val repl = new HarvesterRepl(assembly)
  repl.start()

}

object assembly extends SGHarvesterAssembly {
  override implicit def actorSystem: ActorSystem = ActorSystem("test-login-client")

  override implicit def executionContext: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  override def db: DefaultDB = try {
    val mongoDriver = new MongoDriver()
    val connection = mongoDriver.connection(MongoConnection.parseURI("""mongodb://localhost""").get)
    //    Await.result(connection.database("suicide-girl-repo"), 1 minute)
    Await.result(connection.database("suicide_girls_repo"), 1 minute)
  } catch {
    case e: Throwable =>
      throw new IllegalStateException(s"Failed to initialize Mongo database. Because: ${e.getMessage}", e)
  }
}