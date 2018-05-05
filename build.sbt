import sbt._
import sbt.Keys._

lazy val `scala_2.12`:     String = "2.12.6"
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
        akkaActor,
        //required for package com.lorandszakacs.util.mongodb
        reactiveMongo,
        //required for package com.lorandszakacs.util.time
        nScalaJodaTime,
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

lazy val bmcEffects: ModuleID = "com.busymachines" %% "busymachines-commons-effects" % "0.3.0-RC8" withSources ()

lazy val nScalaJodaTime: ModuleID = "com.github.nscala-time" %% "nscala-time"   % "2.18.0" withSources ()
lazy val reactiveMongo:  ModuleID = "org.reactivemongo"      %% "reactivemongo" % "0.13.0" withSources ()
lazy val typeSafeConfig: ModuleID = "com.typesafe"           % "config"         % "1.3.3"  withSources ()

lazy val logbackClassic: ModuleID = "ch.qos.logback" % "logback-classic" % "1.2.3" withSources ()
lazy val iolog4s:        ModuleID = "org.iolog4s"    %% "iolog4s"        % "0.0.3" withSources ()

lazy val pprint: ModuleID = "com.lihaoyi" %% "pprint" % "0.4.3" withSources ()

lazy val jsoup: ModuleID = "org.jsoup" % "jsoup" % "1.8.1" withSources ()

lazy val parserCombinators: ModuleID = "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.0" withSources ()

//============================================================================================
//================================= http://typelevel.org/scala/ ==============================
//========================================  typelevel ========================================
//============================================================================================
lazy val http4sVersion = "0.18.9"
lazy val http4sDSL     = "org.http4s" %% "http4s-dsl" % http4sVersion
lazy val http4sServer  = "org.http4s" %% "http4s-blaze-server" % http4sVersion
lazy val http4sClient  = "org.http4s" %% "http4s-blaze-client" % http4sVersion

//cats, cats-effect, monix brough in by bmcEffects

//============================================================================================
//================================= http://akka.io/docs/ =====================================
//======================================== akka ==============================================
//============================================================================================

lazy val akkaVersion: String = "2.5.11"

lazy val akkaActor: ModuleID = "com.typesafe.akka" %% "akka-actor" % akkaVersion withSources ()

//============================================================================================
//=========================================  testing =========================================
//============================================================================================

lazy val scalaTest:  ModuleID = "org.scalatest"  %% "scalatest"  % "3.0.5"  % Test withSources ()
lazy val scalaCheck: ModuleID = "org.scalacheck" %% "scalacheck" % "1.13.5" % Test withSources ()

//============================================================================================
//=================================== COMMON-SETTINGS ========================================
//============================================================================================

def commonSettings: Seq[Setting[_]] =
  Seq(
    organization in ThisBuild   := "com.lorandszakacs",
    test in assembly            := {},
    assemblyJarName in assembly := s"${name.value}.jar",
    scalaVersion                := mainScalaVersion,
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.1")
  ) ++ Settings.scalaCompilerFlags
