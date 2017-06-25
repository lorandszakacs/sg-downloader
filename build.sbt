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
)

lazy val `sg-harvester` = Project(
  "sg-harvester",
  base = file("./sg-harvester")
).settings(
  common.buildSettings(projectInfo, common.defaults.lorandszakacsOrg, Option(publishInfo)) ++
  Seq(
    mainClass := Some("com.lorandszakacs.sgd.app.Main"),
    libraryDependencies ++= Seq(
      common.dev.akka.actor,
      common.dev.akka.http,

      common.dev.nScalaJodaTime,
      common.dev.scalaParserCombinators,

      common.dev.logbackClassic,
      common.dev.scalaLogging,

      "com.lorandszakacs" %% "util-html" % "0.1.2" withSources(),

      common.test.scalaTest
    )
  )
).aggregate(
  `sg-model-repo`
).dependsOn(
`sg-model-repo`
)

lazy val `sg-model-repo` = Project(
  "sg-model-repo",
  base = file("./sg-model-repo")
).settings(
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
