package home.sg.app

import home.sg.constants.Version
import home.sg.client.SGClient
import home.sg.repl.Repl

object Main {

  def main(args: Array[String]): Unit = {
    println("SG-downloader " + Version.version)
    println("type -help for instructions")

    val client = new SGClient(true)
    val repl = new Repl(client)
    repl.start
    println("exiting SG-downloader.")
  }

}