package com.lorandszakacs.sg.app.commands

import com.lorandszakacs.sg.model.Name

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

  object DownloadSpecific extends CommandDescription {
    override def id: String = "download"

    override def humanlyReadableDescription: String =
      s"""|Downloads the specified Ms only, updates state to database, and exports them as html
        """.stripMargin.trim()

    override def manDescription: String =
      """download names=X[,Y]* [username=Y password=Z]"""
  }

  case class DownloadSpecific(
    names:               List[Name],
    usernameAndPassword: Option[(String, String)]
  ) extends Command {
    require(names.nonEmpty, "DownloadSpecific names cannot be empty")
  }

  //====================================================================================

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
    days:                Option[Int],
    usernameAndPassword: Option[(String, String)]
  ) extends Command {
    def username: Option[String] = usernameAndPassword.map(_._1)

    def password: Option[String] = usernameAndPassword.map(_._2)
  }

  //====================================================================================

  object ExportHTML extends CommandDescription {
    override val id: String = "export-html"

    override val humanlyReadableDescription: String =
      s"""|Exports all the existing data as HTML. By default it exports all, and favorites only.
          |With the -f flag it exports on the html of favorites
        """.stripMargin.trim()

    override val manDescription: String =
      """export-html [-f]"""

  }

  case class ExportHTML(
    onlyFavorites: Boolean = false
  ) extends Command {}

  //====================================================================================

  object Show extends CommandDescription {
    override val id: String = "show"

    override val humanlyReadableDescription: String =
      s"""|Displays set information for a given name
        """.stripMargin.trim()

    override val manDescription: String =
      """show $NAME"""

  }

  case class Show(
    name: Name
  ) extends Command

  //====================================================================================

  case object Favorites extends Command with CommandDescription {
    override def id: String = "favorites"

    override def humanlyReadableDescription: String = "Displays the favorites"

    override def manDescription: String = "favorites"
  }

  //====================================================================================

  case object Help extends Command with CommandDescription {
    override val id: String = "help"

    override val humanlyReadableDescription: String =
      """
        |Displays all available commands. Useful domain terminology:
        |
        |index - gather only meta-data (names, and photosets), and saves it --- does not require authentication
        |reify - based on the previously indexed data (names, photosets), it gathers the remaining photo links
        |harvest - index + reify
        |export - use integral harvested data to export (currently only HTML)
        |write - SIDE EFFECTUL in DB, this effectively writes to database.
        |download - harvests (index + reify), exports, and writes.
      """.stripMargin.trim()

    override val manDescription: String = "help"
  }

  //====================================================================================

  case object Exit extends Command with CommandDescription {
    override def id: String = "exit"

    override def humanlyReadableDescription: String = "Exits the application"

    override def manDescription: String = "exit"
  }

  //====================================================================================

  lazy val descriptions: List[CommandDescription] = List(
    DownloadSpecific,
    DeltaDownload,
    Show,
    ExportHTML,
    Favorites,
    Help,
    Exit,
  ).sortBy(_.id)
}
