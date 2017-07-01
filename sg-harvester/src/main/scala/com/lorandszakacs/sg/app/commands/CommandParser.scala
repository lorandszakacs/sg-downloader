package com.lorandszakacs.sg.app.commands

import scala.util.parsing.combinator._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
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
        } else {
          scala.util.Failure[Command](new IllegalArgumentException(s"failed to parse all input of: '$s' still have input left: '$next'"))
        }
      case _: NoSuccess =>
        scala.util.Failure[Command](new IllegalArgumentException(s"failed to parse command: $s"))
    }
  }

  private val space: Parser[String] = literal(" ")

  private val spaces: Parser[String] = whiteSpace

  private val everythingUntilSpaceOrEnd: Parser[String] = regex(s"[^ ]+".r)

  private def anyCombinationOptional[P1, P2](p1: Parser[P1], p2: Parser[P2]): Parser[(Option[P1], Option[P2])] = {
    val both: Parser[(Option[P1], Option[P2])] = for {
      v1 <- p1
      _ <- spaces
      v2 <- p2
    } yield (Option(v1), Option(v2))

    val bothReverse: Parser[(Option[P1], Option[P2])] = for {
      v2 <- p2
      _ <- spaces
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

  //===========================================================================
  //================================= DELTA ===================================
  //===========================================================================

  private val deltaUpdateCommandParser: Parser[Commands.DeltaUpdate] = {
    val days: Parser[Int] = for {
      _ <- literal("days=")
      v <- wholeNumber
    } yield v.toInt

    val usernameAndPassword = for {
      _ <- literal("username=")
      username <- everythingUntilSpaceOrEnd
      _ <- spaces
      _ <- literal("password=")
      password <- everythingUntilSpaceOrEnd
    } yield (username, password)

    val maybeDaysMaybeUserAndPass = for {
      _ <- spaces
      maybe <- anyCombinationOptional(days, usernameAndPassword)
    } yield maybe

    for {
      _ <- literal(Commands.DeltaUpdate.id)
      maybe <- maybeDaysMaybeUserAndPass.?
    } yield Commands.DeltaUpdate(
      days = maybe.flatMap(_._1),
      usernameAndPassword = maybe.flatMap(_._2)
    )
  }

  //===========================================================================
  //================================= HELP ===================================
  //===========================================================================

  private val helpCommandParser: Parser[Commands.Help.type] = {
    literal("help") ^^ { _ => Commands.Help }
  }

  //===========================================================================
  //================================= ????? ===================================
  //===========================================================================

  private val rootCommandParser: Parser[Command] =
    deltaUpdateCommandParser |
      helpCommandParser

}
