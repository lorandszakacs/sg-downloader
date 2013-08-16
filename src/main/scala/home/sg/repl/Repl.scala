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