import sbt._
import sbt.Keys._

lazy val `scala_2.12`:     String = "2.12.8"
lazy val mainScalaVersion: String = `scala_2.12`

addCommandAlias("build",   ";compile;test:compile")
addCommandAlias("rebuild", ";clean;update;compile;test:compile")

addCommandAlias("ci", ";rebuild;test")

addCommandAlias("mkJar", ";rebuild;sg-harvester / assembly")

lazy val root = Project(
  "sg-downloader",
  base = file(".")
).settings(commonSettings)
  .aggregate(`sg-harvester`)

lazy val `sg-harvester` = project
  .settings(commonSettings)
  .settings(
    Seq(
      mainClass in (Compile, run) in ThisBuild := Some("com.lorandszakacs.sg.app.Main"),
      mainClass in assembly        := Some("com.lorandszakacs.sg.app.Main"),
      suppressSbtShellNotification := true,
      fork in run                  := true,
      libraryDependencies ++= Seq(
        http4sDSL,
        http4sServer,
        http4sClient,
        parserCombinators,
        scalaTest
      )
    )
  )
  .aggregate(
    `sg-core`,
    `sg-repo`,
    `util`
  )
  .dependsOn(
    `sg-repo`,
    `util`
  )

lazy val `sg-repo` = project
  .settings(commonSettings)
  .settings(
    Seq(
      libraryDependencies ++= Seq(
        scalaTest
      )
    )
  )
  .aggregate(
    `sg-core`,
    `util`
  )
  .dependsOn(
    `sg-core`,
    `util`
  )

lazy val `sg-core` = project
  .settings(commonSettings)

lazy val `util` = project
  .settings(commonSettings)
  .settings(
    Seq(
      libraryDependencies ++= Seq(
        //required for package com.lorandszakacs.util.mongodb
        reactiveMongo,
        logbackClassic,
        iolog4s,
        bmcEffects,
        //required for package com.lorandszakacs.util.html
        jsoup,
        scalaTest
      )
    )
  )

//============================================================================================
//========================================== Misc ============================================
//============================================================================================

lazy val bmcV            = "0.3.0-RC10" //https://github.com/busymachines/busymachines-commons/releases
lazy val reactiveMongoV  = "0.13.0" //https://github.com/ReactiveMongo/ReactiveMongo/releases
lazy val typesafeConfigV = "1.3.3" //https://github.com/lightbend/config/releases
lazy val log4catsV       = "0.0.3" //https://github.com/ChristopherDavenport/log4cats
lazy val logbackV        = "1.2.3" //https://github.com/qos-ch/logback/releases
lazy val jsoupV          = "1.8.1" //https://github.com/jhy/jsoup/releases
lazy val scalaPCV        = "1.1.2" //https://github.com/scala/scala-parser-combinators/releases
lazy val http4sV         = "0.18.9" //https://github.com/http4s/http4s/releases
lazy val scalaTestV      = "3.0.5" //https://github.com/scalatest/scalatest/releases
lazy val scalaCheckV     = "1.13.5" //https://github.com/rickynils/scalacheck/releases

lazy val bmcEffects: ModuleID = "com.busymachines" %% "busymachines-commons-effects" % bmcV withSources ()

lazy val reactiveMongo:  ModuleID = "org.reactivemongo" %% "reactivemongo" % reactiveMongoV  withSources ()
lazy val typeSafeConfig: ModuleID = "com.typesafe"      % "config"         % typesafeConfigV withSources ()

lazy val logbackClassic: ModuleID = "ch.qos.logback" % "logback-classic" % logbackV  withSources ()
lazy val iolog4s:        ModuleID = "org.iolog4s"    %% "iolog4s"        % log4catsV withSources ()

lazy val jsoup: ModuleID = "org.jsoup" % "jsoup" % jsoupV withSources ()

lazy val parserCombinators: ModuleID = "org.scala-lang.modules" %% "scala-parser-combinators" % scalaPCV withSources ()

//============================================================================================
//================================= http://typelevel.org/scala/ ==============================
//========================================  typelevel ========================================
//============================================================================================

lazy val http4sDSL    = "org.http4s" %% "http4s-dsl"          % http4sV
lazy val http4sServer = "org.http4s" %% "http4s-blaze-server" % http4sV
lazy val http4sClient = "org.http4s" %% "http4s-blaze-client" % http4sV

//cats, cats-effect, monix brough in by bmcEffects

//============================================================================================
//=========================================  testing =========================================
//============================================================================================

lazy val scalaTest:  ModuleID = "org.scalatest"  %% "scalatest"  % scalaTestV  % Test withSources ()
lazy val scalaCheck: ModuleID = "org.scalacheck" %% "scalacheck" % scalaCheckV % Test withSources ()

//============================================================================================
//=================================== COMMON-SETTINGS ========================================
//============================================================================================

def commonSettings: Seq[Setting[_]] =
  Seq(
    organization in ThisBuild   := "com.lorandszakacs",
    test in assembly            := {},
    assemblyJarName in assembly := s"${name.value}.jar",
    scalaVersion                := mainScalaVersion,
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.0")
  ) ++ Settings.scalaCompilerFlags
