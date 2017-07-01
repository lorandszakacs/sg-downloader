package com.lorandszakacs.sg.app.commands

import com.lorandszakacs.sg.app.repl.CommandsDepr.{GatherNew, IndexNew}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 01 Jul 2017
  *
  */
sealed trait Command {
  def id: String

  def humanlyReadableDescription: String

  def manDescription: String

  def fullDescription: String =
    s"""|$manDescription
        |
      |$humanlyReadableDescription
  """.stripMargin.trim()
}

object Commands {

  val DeltaUpdateId = "delta"

  case class DeltaUpdate(
    days: Option[Int],
    usernameAndPassword: Option[(String, String)]
  ) extends Command {
    override def id: String = DeltaUpdateId

    override def humanlyReadableDescription: String =
      s"""|Fetches the newest sets (since last time this was run) from the website, updates the state of the database
          |properly, and exports the delta as html.
        """.stripMargin.trim()

    override def manDescription: String =
      """delta [days=X] [username=Y password=Z]"""


    def username: Option[String] = usernameAndPassword.map(_._1)

    def password: Option[String] = usernameAndPassword.map(_._2)
  }

  case object Help extends Command {
    override def id: String = "help"

    override def humanlyReadableDescription: String =
      """
        |Displays all available commands
      """.stripMargin.trim()

    override def manDescription: String = "help"
  }

}

