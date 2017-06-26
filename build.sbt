import sbt._
import sbt.Keys._

import com.lorandszakacs.sbt.commonbuild.plugin.CommonBuildPlugin.{common, _}

lazy val publishInfo = common.defaults.lorandszakacsPublishingInfo(
  ScmInfo(
    browseUrl = url("https://github.com/lorandszakacs/sg-downloader"),
    connection = "git@github.com:lorandszakacs/sg-downloader.git"
  )
)

lazy val projectInfo = ProjectInfo(
  version = "0.1.0-SNAPSHOT"
)

lazy val root = Project(
  "sg-downloader",
  base = file(".")
).settings(
  common.buildSettings(projectInfo, common.defaults.lorandszakacsOrg, Option(publishInfo))
).aggregate(
  `sg-harvester`
).dependsOn(
  `sg-harvester`
)

lazy val `sg-harvester` = project.settings(
  common.buildSettings(projectInfo, common.defaults.lorandszakacsOrg, Option(publishInfo)) ++
  Seq(
    mainClass in (Compile, run) in ThisBuild := Some("com.lorandszakacs.sg.app.Main"),
    libraryDependencies ++= Seq(
      common.dev.akka.actor,
      common.dev.akka.http,

      common.dev.nScalaJodaTime,
      common.dev.scalaParserCombinators,

      common.dev.logbackClassic,
      common.dev.scalaLogging,

      "com.lorandszakacs" %% "util-html" % "0.1.2-SNAPSHOT" withSources(),

      common.test.scalaTest
    )
  )
).aggregate(
  `sg-model-repo`
).dependsOn(
`sg-model-repo`
)

lazy val `sg-model-repo` = project.settings(
  common.buildSettings(projectInfo, common.defaults.lorandszakacsOrg, Option(publishInfo)) ++
  Seq(
    libraryDependencies ++= Seq(
      common.dev.nScalaJodaTime,
      common.dev.reactiveMongo,

      common.dev.logbackClassic,
      common.dev.scalaLogging,
      common.test.scalaTest
    )
  )
)

lazy val `util` = project.settings(
  common.buildSettings(projectInfo, common.defaults.lorandszakacsOrg, Option(publishInfo)) ++
    Seq(
      libraryDependencies ++= Seq(
        //required for package com.lorandszakacs.util.mongodb
        common.dev.reactiveMongo,

        //required for package com.lorandszakacs.util.time
        common.dev.nScalaJodaTime,

        //required for package com.lorandszakacs.util.logging
        common.dev.logbackClassic,
        common.dev.scalaLogging,


        common.test.scalaTest
      )
    )
)
