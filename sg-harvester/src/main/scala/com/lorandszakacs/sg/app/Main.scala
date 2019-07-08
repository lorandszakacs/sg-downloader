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

import com.lorandszakacs.sg.app.repl.{CommandLineInterpreter, REPL}
import com.lorandszakacs.util.effects._
import com.lorandszakacs.util.logger._

/**
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 16 Mar 2015
  *
  */
object Main extends PureharmIOApp {

  override val ioRuntime: Later[(ContextShift[IO], Timer[IO])] = IORuntime.defaultMainRuntime("sg-app-main")

  implicit private val dbIOScheduler:   DBIOScheduler   = DBIOScheduler(UnsafePools.cached("sg-app-dbio"))
  implicit private val httpIOScheduler: HTTPIOScheduler = HTTPIOScheduler(UnsafePools.cached("sg-app-http"))
  implicit private val futureLift:      FutureLift[IO]  = FutureLift.instance[IO](LiftIO[IO], contextShift)

  implicit private val logger: Logger[IO] = Logger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] = {
    val replResource = for {
      assembly <- Resource.pure[IO, Assembly](
        new Assembly()(contextShift, dbIOScheduler, futureLift, httpIOScheduler),
      )
      sgClient <- assembly.sgClient
      interpreter = new CommandLineInterpreter(sgClient, assembly)
    } yield (assembly, interpreter, new REPL(interpreter))

    //TODO: redo all these, and make them resources
    replResource.use { tuple =>
      val (assembly, interpreter, repl) = tuple

      val interpretArgsIO: IO[Unit] =
        logger.info(s"Received args: ${args.mkString(",")} --> executing command") >>
          interpreter.interpretArgs(args).onError {
            case NonFatal(e) =>
              logger.error(e)("—— something went wrong during interpretation —— exiting") >>
                assembly.shutdownIO >>
                IO(System.exit(1))
          }

      val startReplIO = logger.info(s"Did not receive any arguments, going into REPL mode") >>
        repl.runIO

      for {
        _ <- assembly.initIO
        _ <- if (args.isEmpty) startReplIO else interpretArgsIO
        _ <- assembly.shutdownIO
        _ <- IO(println("... finished gracefully"))
      } yield ExitCode.Success
    }
  }

}
