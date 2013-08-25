/**
 * Copyright (c) 2013 Lorand Szakacs
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
package home.sg.repl

import home.sg.parser.command.SGCommandParser
import home.sg.parser.command.Exit
import home.sg.client.SGClient
import home.sg.parser.command.Fail
import home.sg.parser.command.Update
import home.sg.parser.command.UpdateAll
import home.sg.parser.command.DownloadFromFile
import home.sg.parser.command.Download
import home.sg.parser.command.Login
import home.sg.parser.command.CommandVisitorFail

class Repl(client: SGClient) {
  def start(): Unit = {
    val interpreter = new CommandInterpreter(client);
    println("type -help for instructions")
    while (true) {
      print("> ")
      val input = Console.readLine
      val command = SGCommandParser.apply(input);
      command match {
        case Exit() => { client.cleanUp; return }
        case _ => {
          val result = interpreter.visit(command)
          result match {
            case CommandVisitorFail(msg) => println(msg)
            case _ => Unit
          }
        }
      }
    }
  }
}