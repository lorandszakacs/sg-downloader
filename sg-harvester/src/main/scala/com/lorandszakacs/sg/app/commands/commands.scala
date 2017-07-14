package com.lorandszakacs.sg.app.commands

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

  object DeltaDownload extends CommandDescription {
    override val id: String = "delta"

    override val humanlyReadableDescription: String =
      s"""|Downloads the newest sets (since last time this was run) from the website, updates the state of the database
          |properly, and exports the delta as html.
        """.stripMargin.trim()

    override val manDescription: String =
      """delta [days=X] [username=Y password=Z]"""

  }

  case class DeltaDownload(
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
        |Displays all available commands. Useful domain terminology:
        |
        |index - gather only meta-data (model names, and photosets), and saves it --- does not require authentication
        |reify - based on the previously indexed data (model names, photosets), it gathers the remaining photo links
        |harvest - index + reify
        |export - use integral harvested data to export (currently only HTML)
        |write - SIDE EFFECTUL in DB, this effectively writes to database.
        |download - harvests (index + reify), exports, and writes.
        |=====
      """.stripMargin.trim()

    override val manDescription: String = "help"
  }

  case object Exit extends Command with CommandDescription {
    override def id: String = "exit"

    override def humanlyReadableDescription: String = "Exits the application"

    override def manDescription: String = "exit"
  }

  lazy val descriptions: List[CommandDescription] = List(
    DeltaDownload, Help, Exit
  ).sortBy(_.id)
}

