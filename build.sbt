import sbt._
import sbt.Keys._

lazy val `scala_2.12`:     String = "2.12.4"
lazy val mainScalaVersion: String = `scala_2.12`

addCommandAlias("build", ";compile;test:compile")
addCommandAlias("rebuild", ";clean;update;compile;test:compile")

addCommandAlias("ci", ";rebuild;test")

addCommandAlias("mkJar", ";rebuild;sg-harvester / assembly")

lazy val root = Project(
  "sg-downloader",
  base = file(".")
).settings(
    commonSettings
  )
  .aggregate(
    `sg-harvester`
  )

lazy val `sg-harvester` = project
  .settings(
    commonSettings ++
      Seq(
        mainClass in (Compile, run) in ThisBuild := Some("com.lorandszakacs.sg.app.Main"),
        mainClass in assembly                    := Some("com.lorandszakacs.sg.app.Main"),
        suppressSbtShellNotification             := true,
        fork in run                              := true,
        libraryDependencies ++= Seq(
          akkaActor,
          akkaStream,
          akkaHttp,
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
  .settings(
    commonSettings ++
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
  .settings(
    commonSettings
  )

lazy val `util` = project
  .settings(
    commonSettings ++
      Seq(
        libraryDependencies ++= Seq(
          //required for package com.lorandszakacs.util.mongodb
          reactiveMongo,
          //required for package com.lorandszakacs.util.time
          nScalaJodaTime,
          //required for package com.lorandszakacs.util.logging
          logbackClassic,
          scalaLogging,
          catsCore,
          catsEffect,
          //required for package com.lorandszakacs.util.html
          jsoup,
          scalaTest
        )
      )
  )

//============================================================================================
//========================================== Misc ============================================
//============================================================================================

lazy val nScalaJodaTime: ModuleID = "com.github.nscala-time" %% "nscala-time"   % "2.18.0" withSources ()
lazy val reactiveMongo:  ModuleID = "org.reactivemongo"      %% "reactivemongo" % "0.13.0" withSources ()
lazy val typeSafeConfig: ModuleID = "com.typesafe"           % "config"         % "1.3.1" withSources ()

lazy val scalaLogging:   ModuleID = "com.typesafe.scala-logging" %% "scala-logging"  % "3.5.0" withSources ()
lazy val logbackClassic: ModuleID = "ch.qos.logback"             % "logback-classic" % "1.1.7" withSources ()

lazy val pprint: ModuleID = "com.lihaoyi" %% "pprint" % "0.4.3" withSources ()

lazy val jsoup: ModuleID = "org.jsoup" % "jsoup" % "1.8.1" withSources ()

//============================================================================================
//================================= http://typelevel.org/scala/ ==============================
//========================================  typelevel ========================================
//============================================================================================

lazy val shapeless: ModuleID = "com.chuusai" %% "shapeless" % "2.3.2" withSources ()

lazy val catsVersion: String = "1.0.1"

lazy val catsCore:    ModuleID = "org.typelevel" %% "cats-core"    % catsVersion withSources ()
lazy val catsMacros:  ModuleID = "org.typelevel" %% "cats-macros"  % catsVersion withSources ()
lazy val catsKernel:  ModuleID = "org.typelevel" %% "cats-kernel"  % catsVersion withSources ()
lazy val catsLaws:    ModuleID = "org.typelevel" %% "cats-laws"    % catsVersion withSources ()
lazy val catsTestkit: ModuleID = "org.typelevel" %% "cats-testkit" % catsVersion withSources ()

lazy val catsEffect: ModuleID = "org.typelevel" %% "cats-effect" % "0.9" withSources ()

//============================================================================================
//================================= http://akka.io/docs/ =====================================
//======================================== akka ==============================================
//============================================================================================

lazy val akkaVersion: String = "2.5.11"

lazy val akkaActor:  ModuleID = "com.typesafe.akka" %% "akka-actor"  % akkaVersion withSources ()
lazy val akkaStream: ModuleID = "com.typesafe.akka" %% "akka-stream" % akkaVersion withSources ()

lazy val akkaHttpVersion: String   = "10.1.0"
lazy val akkaHttp:        ModuleID = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion withSources ()

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
    scalaVersion                := mainScalaVersion
  ) ++ Settings.scalaCompilerFlags
