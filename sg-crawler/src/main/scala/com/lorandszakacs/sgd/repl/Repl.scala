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
package com.lorandszakacs.sgd.repl

import com.lorandszakacs.sgd.parser.command.{CommandVisitorFail, DefaultCommandInterpreter, Exit, SGCommandParser}

import scala.io.StdIn
import scala.util.control.Breaks

/**
 * @author Lorand Szakacs, lsz@lorandszakacs.com
 * @since 16 Mar 2015
 *
 */
class Repl() {
  def start(): Unit = {
    val interpreter = DefaultCommandInterpreter
    println("type -help for instructions")

    val loop = new Breaks
    loop.breakable {
      print("> ")
      val input = StdIn.readLine()
      SGCommandParser(input) match {
        case Exit() =>
          loop.break()
        case command =>
          val result = interpreter.visit(command)
          result match {
            case CommandVisitorFail(msg) =>
              println(msg)
            case _ => ()
          }
      }
    }
  }
}
