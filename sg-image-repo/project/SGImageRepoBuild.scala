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

import sbt._
import Keys._
import com.lorandszakacs.sbt.commonbuild.plugin.CommonBuildPlugin._

/**
 * @author Lorand Szakacs, lsz@lorandszakacs.com
 * @since 16 Mar 2015
 *
 */
object SGImageRepoBuild extends CommonBuild {

  lazy val sgInfo = RootProject(file("../sg-info"))

  lazy val root = Project(
    "sg-image-repo",
    file("."),
    settings = commonBuildSettings ++
      Seq(
        mainClass := Some("com.lorandszakacs.sgd.app.Main")
      ),
    aggregate = Seq(
      sgInfo
    ),
    dependencies = Seq(
      sgInfo % "compile->compile;test->test"
    )
  )

  override def projectVersion: String = "0.1.0-SNAPSHOT"

  override def devDependencies: Seq[ModuleID] = Seq()

  override def runtimeDependencies: Seq[ModuleID] = Nil

  override def otherDependencies: Seq[ModuleID] = Nil

  override def testDependencies: Seq[ModuleID] = Seq(
    test.scalaTest
  )

  override def pluginSettings: Seq[Setting[_]] = Nil

  override def otherSettings: Seq[Setting[_]] = Nil

  override def publishingInfo: Option[PublishInfo] = None
}
