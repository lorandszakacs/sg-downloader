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
  private val interpreter: CommandLineInterpreter
) extends StrictLogging {

  def runTask: Task[Unit] = {
    for {
      _ <- Task(println("type help for instructions"))
      _ <- loop(stop = false).recoverWith {
        case NonFatal(e) => Task(logger.error("the loop somehow broke. terminating", e))
      }
    } yield ()
  }

  private def loop(stop: Boolean): Task[Unit] = {
    if (stop) {
      Task.unit
    }
    else {
      for {
        _     <- Task(print("> "))
        input <- Task(StdIn.readLine().trim())
        _ <- interpreter.interpret(input).attempt.flatMap {
          case Left(thr) =>
            Task(logger.error(s"failed to interpret command: '$input'", withFilteredStackTrace(thr))) >>
              Task(print("\n")) >>
              loop(stop = false)
          case Right(c) =>
            if (c == Commands.Exit) loop(stop = true) else loop(stop = false)
        }
      } yield ()
    }
  }
  private lazy val banned = Set[String](
    "cats",
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
