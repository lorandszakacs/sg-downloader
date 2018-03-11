package com.lorandszakacs.util

import busymachines.effects.async._
import busymachines.effects.sync._
import com.lorandszakacs.util.list.ListUtilFunctions

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 11 Mar 2018
  *
  */
object effects
    extends AnyRef with OptionSyntax.Implicits with OptionSyntaxAsync.Implcits with TryTypeDefinitons
    with TrySyntax.Implicits with TrySyntaxAsync.Implcits with EitherSyntax.Implicits with EitherSyntaxAsync.Implcits
    with ResultTypeDefinitions with ResultCompanionAliases with ResultSyntax.Implicits with ResultSyntaxAsync.Implcits
    with FutureTypeDefinitions with FutureSyntax.Implicits with IOTypeDefinitions with IOSyntax.Implicits
    with cats.instances.AllInstances with cats.syntax.AllSyntax with ListUtilFunctions {

  val NonFatal: scala.util.control.NonFatal.type = scala.util.control.NonFatal
}
