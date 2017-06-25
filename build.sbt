import sbt._
import sbt.Keys._

import com.lorandszakacs.sbt.commonbuild.plugin.CommonBuildPlugin.{common, _}

lazy val publishInfo = common.defaults.lorandszakacsPublishingInfo(
  ScmInfo(
    browseUrl = url("https://github.com/lorandszakacs/util-html"),
    connection = "git@github.com:lorandszakacs/util-html.git"
  )
)

lazy val projectInfo = ProjectInfo(
  version = "0.1.2"
)

lazy val root = Project(
  id = "util-html",
  base = file(".")
).settings(
  common.buildSettings(projectInfo, common.defaults.lorandszakacsOrg, Option(publishInfo)) ++
  Seq(
    libraryDependencies ++= Seq(
      common.dev.java.jsoup,
      common.test.scalaTest
    )
  )
)
