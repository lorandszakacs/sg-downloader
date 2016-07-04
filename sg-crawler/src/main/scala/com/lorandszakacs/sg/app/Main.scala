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
import akka.http.scaladsl.model.HttpRequest
import com.lorandszakacs.sg.harvester.SGHarvesterAssembly
import com.lorandszakacs.sg.http.PatienceConfig
import com.typesafe.scalalogging.StrictLogging
import reactivemongo.api.{MongoConnection, MongoDriver, DefaultDB}

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 16 Mar 2015
  *
  */
object Main extends App with StrictLogging {
  def shutdown(system: ActorSystem) {
    system.terminate()
  }

  implicit val patienceConfig: PatienceConfig = PatienceConfig(200 millis)
  implicit val ec: ExecutionContext = Assembly.executionContext

  logger.info("Starting harverster")

  logger.info("Gathering all SGs")
  val updateIndex = for {
  //    _ <- Assembly.sgHarvester.updateSGIndex(Int.MaxValue)
    _ <- Assembly.sgHarvester.updateHopefulIndex(Int.MaxValue)
  } yield ()

  Await.result(updateIndex, 6 hours)
  logger.info("exiting!")
  shutdown(Assembly.actorSystem)
  Assembly.db.connection.close()
  System.exit(0)
}

object Assembly extends SGHarvesterAssembly {
  override implicit def actorSystem: ActorSystem = ActorSystem("test-login-client")

  override implicit def executionContext: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  override def authentication: (HttpRequest) => HttpRequest = identity

  override def db: DefaultDB = try {
    val mongoDriver = new MongoDriver()
    val connection = mongoDriver.connection(MongoConnection.parseURI("""mongodb://localhost""").get)
    connection("suicide-girl-repo")
  } catch {
    case e: Throwable =>
      throw new IllegalStateException(s"Failed to initialize Mongo database. Because: ${e.getMessage}", e)
  }
}