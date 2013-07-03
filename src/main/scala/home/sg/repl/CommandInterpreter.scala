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

class CommandInterpreter(client: SGClient) extends CommandVisitor {
  
  
  
  override def visit(fail: Fail): CommandVisitorResult = {
    println("error: %s".format(fail.msg));
    CommandVisitorSuccess()
  }

  override def visit(login: Login): CommandVisitorResult = {
    
    client.login(login.user, login.password)
    
    CommandVisitorFail(ErrorMessages.unimplemented) 
    
  }

  override def visit(update: Update): CommandVisitorResult = { CommandVisitorFail(ErrorMessages.unimplemented) }

  override def visit(updateAll: UpdateAll): CommandVisitorResult = { CommandVisitorFail(ErrorMessages.unimplemented) }

  override def visit(download: Download): CommandVisitorResult = { CommandVisitorFail(ErrorMessages.unimplemented) }

  override def visit(downloadFromFile: DownloadFromFile): CommandVisitorResult = { CommandVisitorFail(ErrorMessages.unimplemented) }

  override def visit(exit: Exit): CommandVisitorResult = { CommandVisitorFail(ErrorMessages.unimplemented) }
}