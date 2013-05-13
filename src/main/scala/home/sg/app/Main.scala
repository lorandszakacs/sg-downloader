package home.sg.app

import home.sg.client.LevelOfReporting
import home.sg.client.Downloader

object Main {
  // --sg [Name*] [--filter Strings*] [--root-folder folder*]
  def main(args: Array[String]) = {
    args.length match {
      case 3 => {
        val sgName = args(0)
        val user = args(1)
        val pwd = args(2)
        val downloader = new Downloader(sgName, user, pwd, new LevelOfReporting(1))
        downloader.download("/Users/lorand/Downloads/temp/")
      }

      case _ => {
        println("Please provide: sgName user pwd\nExiting.")
      }
    }
  }
}

object ParameterParser {

}