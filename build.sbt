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
import Process._
import Keys._

name := "sg-downloader"

organization := "com.lorandszakacs"

version := "0.1"

scalaVersion := "2.11.5"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8")

javacOptions ++= Seq("-encoding", "utf8", "-g")

javaOptions ++= Seq("-Xmx1G")

javaOptions in Test ++= Seq("-Xmx1G")

mainClass := Some("com.lorandszakacs.sgd.app.Main")

//===================================================
//         dependencies for testing libraries
//===================================================
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.4" % "test" withSources()
)

//===================================================
//         dependencies for dev libraries
//===================================================
libraryDependencies ++= Seq(
  "com.lorandszakacs" %% "util-html" % "0.1.0" withSources(),
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.2" withSources(),
  "com.typesafe.akka" %% "akka-actor" % "2.3.8" withSources(),
  "io.spray" %% "spray-can" % "1.3.2" withSources(),
  "io.spray" %% "spray-http" % "1.3.2" withSources(),
  "io.spray" %% "spray-httpx" % "1.3.2" withSources(),
  "io.spray" %% "spray-util" % "1.3.2" withSources(),
  "io.spray" %% "spray-client" % "1.3.2" withSources()
)

//===================================================
//         dependencies for IDE configurations
//===================================================

//required to create the default `sbt` folder structure
EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource + EclipseCreateSrc.Managed

EclipseKeys.withSource := true