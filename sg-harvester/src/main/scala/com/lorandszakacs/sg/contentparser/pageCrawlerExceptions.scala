package com.lorandszakacs.sg.contentparser

import com.lorandszakacs.util.html.Html

final case class HTMLPageDidNotContainAnyPhotoSetLinksException(html: Html)
    extends Exception(
      s"""
         |HTML page did not contain any photo set links:
         |${html.toString}
  """.stripMargin,
    )

final case class SetRepresentationDidNotContainTimeTagException(html: Html)
    extends Exception(
      s"""
         |HTML element contain any information about the date of the set:
         |${html.toString}
  """.stripMargin,
    )

final case class SetRepresentationDidNotContainTitleException(html: Html)
    extends Exception(
      s"""
         |HTML element contain any information about the title of the set:
         |${html.toString}
  """.stripMargin,
    )

final case class SetRepresentationDidNotContainNameException(html: Html)
    extends Exception(
      s"""
         |HTML element contain any information about the name of the M:
         |${html.toString}
  """.stripMargin,
    )

final case class SetRepresentationDidNotContainURLException(html: Html)
    extends Exception(
      s"""
         |HTML element contain any URL links to set:
         |${html.toString}
  """.stripMargin,
    )

final case class DidNotFindAnySGProfileLinksException()
    extends Exception(
      "Did not find any SG profile links",
    )

final case class DidNotFindAnyHFProfileLinksException()
    extends Exception(
      "Did not find any HF profile links",
    )
