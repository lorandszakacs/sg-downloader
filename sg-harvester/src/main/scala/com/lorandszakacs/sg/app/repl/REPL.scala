package com.lorandszakacs.sg.app.repl

import com.lorandszakacs.util.effects._
import com.lorandszakacs.sg.app.commands.Commands
import com.typesafe.scalalogging.StrictLogging

import scala.io.StdIn

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 04 Jul 2016
  *
  */
class REPL(
  interpreter: CommandLineInterpreter
) extends StrictLogging {

  def runIO: IO[Unit] = {
    for {
      _ <- IO(println("type help for instructions"))
      _ <- loop(stop = false).recoverWith {
        case NonFatal(e) => IO(logger.error("the loop somehow broke. terminating", e))
      }
    } yield ()
  }

  private def loop(stop: Boolean): IO[Unit] = {
    if (stop) {
      IO.unit
    }
    else {
      for {
        _     <- IO(print("> "))
        input <- IO(StdIn.readLine().trim())
        _ <- interpreter.interpret(input).attempt.flatMap {
          case Left(thr) =>
            IO(logger.error(s"failed to interpret command: '$input'", withFilteredStackTrace(thr))) >>
              IO(print("\n")) >>
              loop(stop = false)
          case Right(c) =>
            if (c == Commands.Exit) loop(stop = true) else loop(stop = false)
        }
      } yield ()
    }
  }
  private lazy val banned = Set[String](
    "cats.effect",
    "java.util.concurrent",
    "java.lang",
    "scala.",
  )

  private def withFilteredStackTrace(throwable: Throwable): Throwable = {
    def idBanned(stackTraceElement: StackTraceElement): Boolean = {
      banned.exists { b =>
        stackTraceElement.getClassName.startsWith(b)
      }
    }

    val oldStackTrace = throwable.getStackTrace
    val newStackTrace = oldStackTrace.filterNot(idBanned)
    throwable.setStackTrace(newStackTrace)
    throwable
  }

}
