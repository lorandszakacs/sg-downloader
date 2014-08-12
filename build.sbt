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

scalaVersion := "2.11.2"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8")

javacOptions ++= Seq("-encoding", "utf8", "-g")

javaOptions ++= Seq("-Xmx1G")

javaOptions in Test ++= Seq("-Xmx1G")

mainClass := "com.lorandszakacs.sgd.app.Main"

//required to create the default `sbt` folder structure
EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource + EclipseCreateSrc.Managed

//===================================================
//         dependencies for testing libraries
//===================================================
libraryDependencies ++= Seq(
  "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test" withSources()
)

//===================================================
//         dependencies for dev libraries
//===================================================
libraryDependencies ++= Seq(
  "com.lorandszakacs" %% "lorandszakacs-commons" % "0.1-SNAPSHOT" changing(),
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.2",
  "org.apache.httpcomponents" % "httpclient" % "4.3.3" withSources(),
  "com.typesafe" % "config" % "1.2.1" withSources()
)