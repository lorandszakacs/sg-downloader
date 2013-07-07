package home.sg.parser.command

sealed trait CommandVisitorResult
case class CommandVisitorFail(val msg: String) extends CommandVisitorResult
case class CommandVisitorSuccess() extends CommandVisitorResult

trait CommandVisitor {
  def visit(comm: Command): CommandVisitorResult = {
    comm match {
      case e: Exit => this.visit(e)
      case f: Fail => this.visit(f)
      case l: Login => this.visit(l)
      case u: Update => this.visit(u)
      case ua: UpdateAll => this.visit(ua)
      case d: Download => this.visit(d)
      case df: DownloadFromFile => this.visit(df)
      case h: Help => this.visit(h)
    }
  }

  def visit(fail: Fail): CommandVisitorResult

  def visit(login: Login): CommandVisitorResult

  def visit(update: Update): CommandVisitorResult

  def visit(updateAll: UpdateAll): CommandVisitorResult

  def visit(download: Download): CommandVisitorResult

  def visit(downloadFromFile: DownloadFromFile): CommandVisitorResult

  def visit (help: Help): CommandVisitorResult
  
  def visit(exit: Exit): CommandVisitorResult
  
  
}