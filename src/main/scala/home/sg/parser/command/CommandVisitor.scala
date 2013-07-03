package home.sg.parser.command

sealed trait CommandVisitorResult
case class CommandVisitorFail(val msg: String) extends CommandVisitorResult
case class CommandVisitorSuccess() extends CommandVisitorResult

trait CommandVisitor {
  def visit(fail: Fail): CommandVisitorResult

  def visit(login: Login): CommandVisitorResult

  def visit(update: Update): CommandVisitorResult

  def visit(updateAll: UpdateAll): CommandVisitorResult

  def visit(download: Download): CommandVisitorResult

  def visit(downloadFromFile: DownloadFromFile): CommandVisitorResult
  
  def visit(exit: Exit): CommandVisitorResult
}