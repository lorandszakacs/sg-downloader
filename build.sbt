// The MIT License (MIT)
//
// Copyright (c) 2014 Lorand Szakacs
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

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