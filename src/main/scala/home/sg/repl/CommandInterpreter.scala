package home.sg.repl

import home.sg.parser.command.CommandVisitor
import home.sg.parser.command.Fail
import home.sg.parser.command.Login
import home.sg.parser.command.Update
import home.sg.parser.command.UpdateAll
import home.sg.parser.command.Download
import home.sg.parser.command.DownloadFromFile
import home.sg.parser.command.Exit
import home.sg.parser.command.CommandVisitorResult
import home.sg.parser.command.CommandVisitorFail
import home.sg.parser.command.CommandVisitorSuccess
import home.sg.client.SGClient
import home.sg.constants.Constants
import home.sg.parser.command.Command
import home.sg.client.LoginUnknownException
import home.sg.client.LoginInvalidUserOrPasswordExn
import home.sg.client.Downloader
import home.sg.client.LevelOfReporting
import home.sg.client.FileDownloadException
import home.sg.client.LoginConnectionLostException
import home.sg.client.HttpClientException
import home.sg.client.UnknownSGException
import home.sg.parser.command.CommandVisitorFail
import home.sg.parser.command.CommandVisitorSuccess
import home.sg.util.IO

class CommandInterpreter(client: SGClient) extends CommandVisitor {

//  override def visit(comm: Command): CommandVisitorResult = {
//    println(comm.instructions)
//    super.visit(comm);
//  }

  override def visit(fail: Fail): CommandVisitorResult = {
    //This one should not be called as the code is right now.
    println("error: %s".format(fail.msg));
    CommandVisitorSuccess()
  }

  override def visit(login: Login): CommandVisitorResult = {
    try {
      client.login(login.user, login.password)
      println("login successful.")
      CommandVisitorSuccess()
    } catch {
      case unknown: LoginUnknownException => CommandVisitorFail(unknown.msg)
      case invalidLog: LoginInvalidUserOrPasswordExn => CommandVisitorFail(invalidLog.msg)
    }
  }

  override def visit(update: Update): CommandVisitorResult = {
    val result = downloadAndReport(startMessageUpdateString, endMessageString, update.folderPath, update.sgs);
    result
  }

  override def visit(updateAll: UpdateAll): CommandVisitorResult = {
    IO.listContent(updateAll.folderPath)
    val allSGFolders = IO.listFolders(updateAll.folderPath)
    val sgNames = allSGFolders map IO.getFileName
    val result = downloadAndReport(startMessageUpdateString, endMessageString, updateAll.folderPath, sgNames)
    result
  }

  override def visit(download: Download): CommandVisitorResult = {
    val result = downloadAndReport(startMessageDownloadString, endMessageString, download.folderPath, download.sgs);
    result
  }

  override def visit(downloadFromFile: DownloadFromFile): CommandVisitorResult = {
    val sgs = IO.readLines(downloadFromFile.filePath);
    val result = downloadAndReport(startMessageDownloadString, endMessageString, downloadFromFile.folderPath, sgs);
    result
  }

  override def visit(exit: Exit): CommandVisitorResult = { CommandVisitorFail(ErrorMessages.unimplemented) }

  private def downloadAndReport(startMsg: String, endMsg: String, folderPath: String, sgs: List[String]) = {
    try {
      val downloaders = sgs map { sg => new Downloader(sg, client, new LevelOfReporting(2)) }
      downloaders map { d =>
        println(startMsg.format(d.sgName))
        d.download(folderPath)
        println(endMsg.format(d.sgName))
      }
      CommandVisitorSuccess();
    } catch {
      case FileDownloadException(msg) => CommandVisitorFail("Trouble with file download: " + msg + "\nExiting.")
      case LoginConnectionLostException(msg) => CommandVisitorFail("LoginConnectionLostException: " + msg + "\nExiting")
      case HttpClientException(msg) => CommandVisitorFail("HttpClientException:  " + msg + "\nExiting")
      case UnknownSGException(msg) => CommandVisitorFail("UnknownSGException:  " + msg + "\nExiting")
      case t: Throwable => CommandVisitorFail("some really unknown shit happened here: " + t.getMessage())
    }
  }

  private val startMessageUpdateString = "updating: %s\n==========================="
  private val startMessageDownloadString = "downloading: %s\n==========================="
  private val endMessageString = "finished: %s\n==========================="
}