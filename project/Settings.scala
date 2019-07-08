import sbt._
import Keys._
import sbtassembly.AssemblyKeys._

object Settings {

  /**
    * tpolecat's glorious compile flag list:
    * https://tpolecat.github.io/2017/04/25/scalac-flags.html
    */
  def scalaCompilerFlags: Seq[Setting[_]] = Seq(
    scalacOptions ++= Seq(
      "-deprecation", // Emit warning and location for usages of deprecated APIs.
      "-encoding",
      "utf-8",                            // Specify character encoding used by source files.
      "-explaintypes",                    // Explain type errors in more detail.
      "-feature",                         // Emit warning and location for usages of features that should be imported explicitly.
      "-language:existentials",           // Existential types (besides wildcard types) can be written and inferred
      "-language:higherKinds",            // Allow higher-kinded types
      "-language:implicitConversions",    // Allow definition of implicit functions called views
      "-unchecked",                       // Enable additional warnings where generated code depends on assumptions.
      "-Xcheckinit",                      // Wrap field accessors to throw an exception on uninitialized access.
      "-Xlint:adapted-args",              // Warn if an argument list is modified to match the receiver.
      "-Xlint:constant",                  // Evaluation of a constant arithmetic expression results in an error.
      "-Xlint:delayedinit-select",        // Selecting member of DelayedInit.
      "-Xlint:doc-detached",              // A Scaladoc comment appears to be detached from its element.
      "-Xlint:inaccessible",              // Warn about inaccessible types in method signatures.
      "-Xlint:infer-any",                 // Warn when a type argument is inferred to be `Any`.
      "-Xlint:missing-interpolator",      // A string literal appears to be missing an interpolator id.
      "-Xlint:nullary-override",          // Warn when non-nullary `def f()' overrides nullary `def f'.
      "-Xlint:option-implicit",           // Option.apply used implicit view.
      "-Xlint:package-object-classes",    // Class or object defined in package object.
      "-Xlint:poly-implicit-overload",    // Parameterized overloaded implicit methods are not visible as view bounds.
      "-Xlint:private-shadow",            // A private field (or class parameter) shadows a superclass field.
      "-Xlint:stars-align",               // Pattern sequence wildcard must align with sequence component.
      "-Xlint:type-parameter-shadow",     // A local type parameter shadows a type already in scope.
      "-Ywarn-extra-implicit",            // Warn when more than one implicit parameter section is defined.
      "-Ywarn-numeric-widen",             // Warn when numerics are widened.
      "-Ywarn-unused:implicits",          // Warn if an implicit parameter is unused.
      "-Ywarn-unused:imports",            // Warn if an import selector is not referenced.
      "-Ywarn-unused:locals",             // Warn if a local definition is unused.
      "-Ywarn-unused:params",             // Warn if a value parameter is unused.
      "-Ywarn-unused:patvars",            // Warn if a variable bound in a pattern is unused.
      "-Ywarn-unused:privates",           // Warn if a private member is unused.
      "-Ywarn-value-discard",             // Warn when non-Unit expression results are unused.

      //"-Xfatal-warnings",

      /*
       * These are flags specific to the "better-monadic-for" plugin:
       * https://github.com/oleg-py/better-monadic-for
       */
      "-P:bm4:no-filtering:y",      // see https://github.com/oleg-py/better-monadic-for#desugaring-for-patterns-without-withfilters--pbm4no-filteringy
      "-P:bm4:no-map-id:y",         // see https://github.com/oleg-py/better-monadic-for#final-map-optimization--pbm4no-map-idy
      "-P:bm4:no-tupling:y",        // see https://github.com/oleg-py/better-monadic-for#desugar-bindings-as-vals-instead-of-tuples--pbm4no-tuplingy
      "-P:bm4:implicit-patterns:y", //see https://github.com/oleg-py/better-monadic-for#define-implicits-in-for-comprehensions-or-matches
    ),
  )
}
