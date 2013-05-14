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

private class Args(val update: Boolean, val user: String, val pwd: String, val downloadPath: String, val sgs: List[String]) {
}

private object Args {
  private val updateFlag = "--u"
  private val passwordFlag = "--p"

  private object PropertyKeys {
    val user = "sg-downloader.user"
    val pwd = "sg-downloader.pwd"
    val tempDownloadPath = "sg-downloader.temp-download-path"
    val updatePath = "sg-downloader.update-path"
  }

  def parseArgs(args: Array[String]): Args = {
    val update = args.contains(updateFlag)
    val conf = ConfigFactory.load()
    val user = conf.getString(PropertyKeys.user)

    val pwd = args.filter(a => a.contains(passwordFlag))
    assume(pwd.length == 1, "You specified too many, or no password flags")
    val password = pwd.head.takeRight(pwd.head.length - passwordFlag.length)

    val downloadPath = if (update) conf.getString(PropertyKeys.updatePath) else conf.getString(PropertyKeys.tempDownloadPath)
    val sgs = args.filterNot(s => s.contains(updateFlag) || s.contains(passwordFlag)).toList
    new Args(update, user, password, downloadPath, sgs)
  }
}

object Main {

  def main(args: Array[String]) = {
    args.length match {
      case 0 => println("invalid arguments, please write: sgNames* [--u] --pPwd")
      case _ => {

        val parsedArgs = Args.parseArgs(args)
        val sgClient = new SGClient(true)

        try {

          val startMessageString = if (parsedArgs.update) "updating: %s\n===========================" else "downloading: %s\n==========================="
          val endMessageString = "finished: %s\n==========================="

          sgClient.login(parsedArgs.user, parsedArgs.pwd)
          val downloaders = parsedArgs.sgs map { sg => new Downloader(sg, sgClient, new LevelOfReporting(2)) }
          downloaders foreach { d =>
            println(startMessageString.format(d.sgName))
            d.download(parsedArgs.downloadPath)
            println(endMessageString.format(d.sgName))
          }
        } catch {
          case sgExn: SGException => {
            sgExn match {
              case FileDownloadException(msg) => reportError("Trouble with file download: " + msg + "\nExiting.")

              case LoginInvalidUserOrPasswordExn(msg) => reportError("Login Invalid user name :" + msg + "\nExiting")
              case LoginConnectionLostException(msg) => reportError("LoginConnectionLostException: " + msg + "\nExiting")
              case LoginUnknownException(msg) => reportError("LoginConnectionLostException:  " + msg + "\nExiting")

              case HttpClientException(msg) => reportError("HttpClientException:  " + msg + "\nExiting")
              case UnknownSGException(msg) => reportError("UnknownSGException:  " + msg + "\nExiting")
              case _ => reportError("some really unknown shit happened here")
            }
          }
          case ex: Exception => reportError("What the fuck?: " + ex.getStackTraceString + "\nExiting.")
        } finally {
          sgClient.cleanUp()
        }
      }
    }
  }

  private def reportError(s: String) = System.err.println(s)

}