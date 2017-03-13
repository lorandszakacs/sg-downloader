/**
  * Copyright 2015 Lorand Szakacs
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */

import com.lorandszakacs.sbt.commonbuild.plugin.CommonBuildPlugin._
import sbt.Keys._
import sbt._

/**
  * @author Lorand Szakacs, lsz@lorandszakacs.com
  * @since 16 Mar 2015
  *
  */
object SGHarvesterBuild extends CommonBuild {

  lazy val sgModel = RootProject(file("../sg-model-repo"))

  lazy val root = Project(
    "sg-harvester",
    file("."),
    settings = commonBuildSettings ++
      Seq(
        mainClass := Some("com.lorandszakacs.sgd.app.Main")
      ),
    aggregate = Seq(
      sgModel
    ),
    dependencies = Seq(
      sgModel % "compile->compile;test->test"
    )
  )

  override def projectVersion: String = "0.1.0-SNAPSHOT"

  override def devDependencies: Seq[ModuleID] = Seq(
    dev.akkaActor,
    dev.akkaHttpCore,

    dev.nScalaJodaTime,
    dev.scalaParserCombinators,

    dev.logbackClassic,
    dev.scalaLogging,

    "com.lorandszakacs" %% "util-html" % "0.1.1" withSources(),
    "com.lorandszakacs" %% "util-io" % "0.1.0" withSources()
  )

  override def runtimeDependencies: Seq[ModuleID] = Nil

  override def otherDependencies: Seq[ModuleID] = Nil

  override def testDependencies: Seq[ModuleID] = Seq(
    test.scalaTest
  )

  override def pluginSettings: Seq[Setting[_]] = Nil

  override def otherSettings: Seq[Setting[_]] = Seq(
    //FIXME: run fails with NullPointerException from sbt with this flag :/
    //    fork in run := true,
    javaOptions in run ++= Seq(
      "-Xms1G", "-Xmx8G", "-XX:+UseConcMarkSweepGC"
    )
  )

  override def publishingInfo: Option[PublishInfo] = None
}
