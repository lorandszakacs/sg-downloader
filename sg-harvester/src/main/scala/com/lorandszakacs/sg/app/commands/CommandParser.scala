package com.lorandszakacs.sg.app.commands

import com.lorandszakacs.sg.model.Name

import scala.util.parsing.combinator._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 01 Jul 2017
  *
  */
object CommandParser extends JavaTokenParsers {

  override val skipWhitespace: Boolean = false

  def parseCommand(s: String): scala.util.Try[Command] = {
    parseAll(rootCommandParser, s.trim) match {
      case Success(result, next) =>
        if (next.atEnd) {
          scala.util.Success[Command](result)
        }
        else {
          scala.util.Failure[Command](
            new IllegalArgumentException(s"failed to parse all input of: '$s' still have input left: '$next'"),
          )
        }
      case _: NoSuccess =>
        scala.util.Failure[Command](new IllegalArgumentException(s"failed to parse command: '$s'"))
    }
  }

  private val `space+` : Parser[String] = whiteSpace

  private val `space*` : Parser[String] = regex("[ ]*".r)

  private val everythingUntilSpaceOrEnd: Parser[String] = regex(s"[^ ]+".r)

  private def anyCombinationOptional[P1, P2](p1: Parser[P1], p2: Parser[P2]): Parser[(Option[P1], Option[P2])] = {
    val both: Parser[(Option[P1], Option[P2])] = for {
      v1 <- p1
      _  <- `space+`
      v2 <- p2
    } yield (Option(v1), Option(v2))

    val bothReverse: Parser[(Option[P1], Option[P2])] = for {
      v2 <- p2
      _  <- `space+`
      v1 <- p1
    } yield (Option(v1), Option(v2))

    val p1NotP2: Parser[(Option[P1], Option[P2])] = for {
      v1 <- p1
    } yield (Option(v1), None)

    val p2NotP1: Parser[(Option[P1], Option[P2])] = for {
      v2 <- p2
    } yield (None, Option(v2))

    for {
      all <- both | bothReverse | p1NotP2 | p2NotP1
    } yield all
  }

  private val usernameAndPassword: Parser[(String, String)] = {
    for {
      _        <- literal("username=")
      username <- everythingUntilSpaceOrEnd
      _        <- `space+`
      _        <- literal("password=")
      password <- everythingUntilSpaceOrEnd
    } yield (username, password)
  }

  private val name: Parser[Name] = {
    regex(s"[^, ]+".r).map(Name.apply)
  }

  //===========================================================================
  //================================= DELTA ===================================
  //===========================================================================

  private val deltaHarvestCommandParser: Parser[Commands.DeltaDownload] = {
    val days: Parser[Int] = for {
      _ <- literal("days=")
      v <- wholeNumber
    } yield v.toInt

    val maybeDaysMaybeUserAndPass = for {
      _     <- `space+`
      maybe <- anyCombinationOptional(days, usernameAndPassword)
    } yield maybe

    for {
      _     <- literal(Commands.DeltaDownload.id)
      maybe <- maybeDaysMaybeUserAndPass.?
    } yield Commands.DeltaDownload(
      days                = maybe.flatMap(_._1),
      usernameAndPassword = maybe.flatMap(_._2),
    )
  }

  //===========================================================================
  //================================ DOWNLOAD =================================
  //===========================================================================

  private val downloadSpecificCommandParser: Parser[Commands.DownloadSpecific] = {
    val namesParser: Parser[List[Name]] = for {
      _     <- literal("names=")
      names <- repsep(name, literal(","))
    } yield names

    for {
      _         <- literal(Commands.DownloadSpecific.id)
      _         <- `space+`
      names     <- namesParser
      _         <- `space*`
      optUsrPwd <- usernameAndPassword.?
    } yield Commands.DownloadSpecific(
      names               = names,
      usernameAndPassword = optUsrPwd,
    )
  }

  //===========================================================================
  //================================= EXPORT ==================================
  //===========================================================================

  private val exportHTMLCommandParser: Parser[Commands.ExportHTML] = {
    for {
      _ <- literal(Commands.ExportHTML.id)
      f <- (`space*` ~> literal("-f")).?
    } yield Commands.ExportHTML(
      onlyFavorites = f.isDefined,
    )

  }

  //===========================================================================
  //============================= FAVORITES ===================================
  //===========================================================================

  private val favoritesCommandParser: Parser[Commands.Favorites.type] = {
    literal(Commands.Favorites.id) ^^ { _ =>
      Commands.Favorites
    }
  }

  //===========================================================================
  //================================= SHOW ===================================
  //===========================================================================

  private val showCommandParser: Parser[Commands.Show] = {
    for {
      _    <- literal(Commands.Show.id)
      _    <- `space+`
      name <- name
    } yield Commands.Show(name)
  }

  //===========================================================================
  //================================= DELETE ==================================
  //===========================================================================

  private val deleteCommandParser: Parser[Commands.Delete] = {
    for {
      _    <- literal(Commands.Delete.id)
      _    <- `space+`
      name <- name
    } yield Commands.Delete(name)
  }

  //===========================================================================
  //================================= HELP ===================================
  //===========================================================================

  private val helpCommandParser: Parser[Commands.Help.type] = {
    literal(Commands.Help.id) ^^ { _ =>
      Commands.Help
    }
  }

  //===========================================================================
  //================================= EXIT ====================================
  //===========================================================================

  private val exitCommandParser: Parser[Commands.Exit.type] = {
    literal(Commands.Exit.id) ^^ { _ =>
      Commands.Exit
    }
  }

  //===========================================================================
  //================================= ????? ===================================
  //===========================================================================

  private val rootCommandParser: Parser[Command] =
    deltaHarvestCommandParser |
      downloadSpecificCommandParser |
      exportHTMLCommandParser |
      deleteCommandParser |
      showCommandParser |
      favoritesCommandParser |
      helpCommandParser |
      exitCommandParser

}
