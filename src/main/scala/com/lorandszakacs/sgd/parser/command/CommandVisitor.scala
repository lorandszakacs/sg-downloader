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
package com.lorandszakacs.sgd.parser.command

sealed trait CommandVisitorResult
case class CommandVisitorFail(val msg: String) extends CommandVisitorResult
case class CommandVisitorSuccess() extends CommandVisitorResult

trait CommandVisitor {
  final def visit(comm: Command): CommandVisitorResult = {
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

  def visit(help: Help): CommandVisitorResult

  def visit(exit: Exit): CommandVisitorResult
}

object DefaultCommandInterpreter extends CommandVisitor {

  override def visit(fail: Fail): CommandVisitorResult = ???

  override def visit(login: Login): CommandVisitorResult = ???

  override def visit(update: Update): CommandVisitorResult = ???

  override def visit(updateAll: UpdateAll): CommandVisitorResult = ???

  override def visit(download: Download): CommandVisitorResult = ???

  override def visit(downloadFromFile: DownloadFromFile): CommandVisitorResult = ???

  override def visit(help: Help): CommandVisitorResult = ???

  override def visit(exit: Exit): CommandVisitorResult = ???
}