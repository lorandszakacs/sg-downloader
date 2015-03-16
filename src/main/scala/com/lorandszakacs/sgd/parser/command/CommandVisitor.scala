/**
 * Copyright 2015 Lorand Szakacs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.lorandszakacs.sgd.parser.command

/**
 * @author Lorand Szakacs, lsz@lorandszakacs.com
 * @since 16 Mar 2015
 *
 */
sealed trait CommandVisitorResult
case class CommandVisitorFail(msg: String) extends CommandVisitorResult
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