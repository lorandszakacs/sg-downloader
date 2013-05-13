package home.sg.app

import home.sg.client.LevelOfReporting
import home.sg.client.Downloader
import com.typesafe.config.ConfigFactory
import home.sg.client.SGException
import home.sg.client.FileDownloadException
import home.sg.client.LoginInvalidUserOrPasswordExn
import home.sg.client.LoginConnectionLostException
import home.sg.client.LoginUnknownException
import home.sg.client.HttpClientException
import home.sg.client.UnknownSGException
import home.sg.client.SGClient

object Main {

  def main(args: Array[String]) = {
    args.length match {
      case 0 => println("invalid arguments, please write: sgNames* [--u]")
      case _ => {
        val update = args.contains("--u")
        val conf = ConfigFactory.load()
        val user = conf.getString(PropertyKeys.user)
        val pwd = conf.getString(PropertyKeys.pwd)

        val downloadPath = if (update) conf.getString(PropertyKeys.updatePath) else conf.getString(PropertyKeys.tempDownloadPath)
        val sgs = args.filterNot(_.equals("--u"))

        val sgClient = new SGClient(true)

        try {

          val startMessageString = if (update) "updating: %s\n===========================" else "downloading: %s\n==========================="
          val endMessageString = "finished: %s\n==========================="

          sgClient.login(user, pwd)
          val downloaders = sgs map { sg => new Downloader(sg, sgClient, new LevelOfReporting(2)) }
          downloaders foreach { d =>
            println(startMessageString.format(d.sgName))
            d.download(downloadPath)
            println(endMessageString.format(d.sgName))
          }
        } catch {
          case sgExn: SGException => {
            sgExn match {
              case FileDownloadException(msg) => reportError("Trouble with file download: " + msg + "\nExiting.")

              case LoginInvalidUserOrPasswordExn(msg) => reportError(msg + "\nExiting")
              case LoginConnectionLostException(msg) => reportError(msg + "\nExiting")
              case LoginUnknownException(msg) => reportError(msg + "\nExiting")

              case HttpClientException(msg) => reportError(msg + "\nExiting")
              case UnknownSGException(msg) => reportError(msg + "\nExiting")
              case _ => reportError("some really unknown shit happened here")
            }
          }
          case ex: Exception => reportError(ex.getMessage() + "\nExiting.")
        } finally {
          sgClient.cleanUp()
        }
      }
    }
  }

  private def reportError(s: String) = System.err.println(s)

}

private object PropertyKeys {
  val user = "sg-downloader.user"
  val pwd = "sg-downloader.pwd"
  val tempDownloadPath = "sg-downloader.temp-download-path"
  val updatePath = "sg-downloader.update-path"
}