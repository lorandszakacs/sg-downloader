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

import akka.actor.{ActorSystem, PoisonPill}
import com.lorandszakacs.sg.app.repl.HarvesterRepl
import com.lorandszakacs.sg.exporter.ModelDisplayerAssembly
import com.lorandszakacs.sg.harvester.SGHarvesterAssembly
import com.lorandszakacs.sg.model.SGModelAssembly
import com.lorandszakacs.util.monads.future.FutureUtil._
import com.typesafe.scalalogging.StrictLogging
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.language.postfixOps
import scala.util.Try

/**
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 16 Mar 2015
  *
  */
object Main extends App with StrictLogging {

  val repl = new HarvesterRepl(assembly)
  repl.start()
  Await.result(assembly.shutdown(), 10 seconds)
  println("... finished gracefully")

}

object assembly extends SGHarvesterAssembly with ModelDisplayerAssembly with SGModelAssembly with StrictLogging {
  override implicit lazy val actorSystem: ActorSystem = ActorSystem("sg-app")

  override implicit lazy val executionContext: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  override lazy val db: DefaultDB = _dataBase.get

  private lazy val _mongoDriver: MongoDriver = new MongoDriver()
  private lazy val _dataBase: Try[DefaultDB] = {
    val future = {
      val _mongoConnection: MongoConnection = _mongoDriver.connection(MongoConnection.parseURI("""mongodb://localhost""").get)
      _mongoConnection.database("suicide_girls_repo")
    } recover {
      case e: Throwable =>
        throw new IllegalStateException(s"Failed to initialize Mongo database. Because: ${e.getMessage}", e)
    }
    Try(Await.result(future, 10 seconds))
  }

  def shutdown(): Future[Unit] = {
    logger.info(s"attempting to shut down: ${_mongoDriver.numConnections} connections")

    for {
      _ <- Future.traverse(_mongoDriver.connections) { connection =>
        logger.info(s"asking: _mongoDriver.connection to close. ${connection.name}")
        for {
          _ <- connection.askClose()(2 seconds) map { _ =>
            logger.info(s"terminated -- connection: ${connection.name}")
          }
          //propably doesn't do anything
          _ = connection.actorSystem.actorSelection("*") ! PoisonPill
        } yield ()

      }

      _ <- _mongoDriver.system.terminate() map { _ =>
        logger.info("terminated -- _mongoDriver.system.terminate()")
      }

      _ <- actorSystem.terminate() map { _ =>
        logger.info("terminated -- actorSystem.terminate()")
      }

    } yield {
      logger.info("terminated -- completed assembly.shutdown()")
    }
  }

}