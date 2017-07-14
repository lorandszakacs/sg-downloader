package com.lorandszakacs.sg.app.commands

import com.lorandszakacs.sg.model.ModelName

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
      s"""|Downloads the specified models only, updates state to database, and exports them as html
        """.stripMargin.trim()

    override def manDescription: String =
      """download models=X[,Y]* [username=Y password=Z]"""
  }

  case class DownloadSpecific(
    models: List[ModelName],
    usernameAndPassword: Option[(String, String)]
  ) extends Command {
    require(models.nonEmpty, "DownloadSpecific models cannot be empty")
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
    days: Option[Int],
    usernameAndPassword: Option[(String, String)]
  ) extends Command {
    def username: Option[String] = usernameAndPassword.map(_._1)

    def password: Option[String] = usernameAndPassword.map(_._2)
  }

  //====================================================================================

  object Show extends CommandDescription {
    override val id: String = "show"

    override val humanlyReadableDescription: String =
      s"""|Displays set information for a given model
        """.stripMargin.trim()

    override val manDescription: String =
      """show $MODEL_NAME"""

  }

  case class Show(
    modelName: ModelName
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

  //====================================================================================

  case object Exit extends Command with CommandDescription {
    override def id: String = "exit"

    override def humanlyReadableDescription: String = "Exits the application"

    override def manDescription: String = "exit"
  }

  //====================================================================================

  lazy val descriptions: List[CommandDescription] = List(
    DownloadSpecific, DeltaDownload, Show, Favorites, Help, Exit
  ).sortBy(_.id)
}

