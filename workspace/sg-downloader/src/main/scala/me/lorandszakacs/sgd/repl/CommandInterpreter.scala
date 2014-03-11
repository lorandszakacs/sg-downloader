/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2014 Lorand Szakacs
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
package me.lorandszakacs.sgd.repl

import me.lorandszakacs.sgd.client.Downloader
import me.lorandszakacs.sgd.parser.command.CommandVisitor
import me.lorandszakacs.sgd.parser.command.CommandVisitorFail
import me.lorandszakacs.sgd.parser.command.CommandVisitorResult
import me.lorandszakacs.sgd.parser.command.CommandVisitorSuccess
import me.lorandszakacs.sgd.parser.command.Download
import me.lorandszakacs.sgd.parser.command.DownloadFromFile
import me.lorandszakacs.sgd.parser.command.Exit
import me.lorandszakacs.sgd.parser.command.Fail
import me.lorandszakacs.sgd.parser.command.Help
import me.lorandszakacs.sgd.parser.command.Login
import me.lorandszakacs.sgd.parser.command.Update
import me.lorandszakacs.sgd.parser.command.UpdateAll
import me.lorandszakacs.util.http.FileDownloadException
import me.lorandszakacs.util.http.HttpClientException
import me.lorandszakacs.util.http.LoginConnectionLostException
import me.lorandszakacs.util.http.LoginInvalidUserOrPasswordExn
import me.lorandszakacs.util.http.LoginUnknownException
import me.lorandszakacs.util.http.SGClient
import me.lorandszakacs.util.http.UnknownSGException
import me.lorandszakacs.util.io.IO

class CommandInterpreter(client: SGClient) extends CommandVisitor {

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

  override def visit(help: Help): CommandVisitorResult = {
    //TODO: implement help
    val commands = List(Login("", ""), Update(List(""), ""), UpdateAll(""),
      Download(List(""), ""), DownloadFromFile("", ""), Exit())
    commands foreach { c => println("    " + c.man) }
    CommandVisitorSuccess()
  }

  override def visit(exit: Exit): CommandVisitorResult = { CommandVisitorFail(ErrorMessages.Unimplemented) }

  private def downloadAndReport(startMsg: String, endMsg: String, folderPath: String, sgs: List[String]) = {
    try {
      val downloaders = sgs map { sg => new Downloader(sg, client) }
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