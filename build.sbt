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

def subProject(sbpr: String) = file("workspace/" + sbpr)

//order of aggregation doesn't matter
lazy val aggregatingProject = project.in(file(".")).aggregate(utilTest, utilIO, utilHttp, utilHtml, sgDownloader)

lazy val utilTest = project.in(subProject(SubProjects.Names.UtilTest))

lazy val utilIO = project.in(subProject(SubProjects.Names.UtilIO)).dependsOn(utilTest % "test->test")

lazy val utilHttp = project.in(subProject(SubProjects.Names.UtilHttp)).dependsOn(utilTest % "test->test", utilIO, utilHtml)

lazy val utilHtml = project.in(subProject(SubProjects.Names.UtilHtml)).dependsOn(utilTest % "test->test")

lazy val sgDownloader = project.in(subProject(SubProjects.Names.SgDownloader)).dependsOn(utilTest % "test->test", utilHttp, utilHtml, utilIO)

name := "SG Downloader and Manager"

organization := Common.organization

version := "0.2"

scalaVersion := Common.scalaVersion

scalacOptions ++= Common.scalacOptions

javaOptions ++= Common.javaOptions

javaOptions in Test ++= Testing.javaOptions

mainClass in Compile := (mainClass in sgDownloader in Compile).value

fullClasspath in Runtime := (fullClasspath in sgDownloader in Runtime).value

sourceDirectories := Seq()
