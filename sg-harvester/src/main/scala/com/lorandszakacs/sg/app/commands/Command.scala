package com.lorandszakacs.sg.app.commands

import com.lorandszakacs.sg.app.repl.CommandsDepr.{GatherNew, IndexNew}

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 01 Jul 2017
  *
  */
sealed trait Command

sealed trait CommandDescription {
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

  object DeltaUpdate extends CommandDescription {
    override val id: String = "delta"

    override val humanlyReadableDescription: String =
      s"""|Fetches the newest sets (since last time this was run) from the website, updates the state of the database
          |properly, and exports the delta as html.
        """.stripMargin.trim()

    override val manDescription: String =
      """delta [days=X] [username=Y password=Z]"""

  }

  case class DeltaUpdate(
    days: Option[Int],
    usernameAndPassword: Option[(String, String)]
  ) extends Command {
    def username: Option[String] = usernameAndPassword.map(_._1)

    def password: Option[String] = usernameAndPassword.map(_._2)
  }

  case object Help extends Command with CommandDescription {
    override val id: String = "help"

    override val humanlyReadableDescription: String =
      """
        |Displays all available commands
      """.stripMargin.trim()

    override val manDescription: String = "help"
  }

  lazy val descriptions: List[CommandDescription] = List(
    DeltaUpdate, Help
  ).sortBy(_.id)
}

