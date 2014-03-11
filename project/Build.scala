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
import Keys._

object SubProjects{
  object Names{
    val UtilIO = "util-io"
    val UtilHtml = "util-html"
    val UtilHttp = "util-http"
    val UtilTest = "util-test"
    val SgDownloader = "sg-downloader"

  }
  
}

object Common {
  lazy val organization = "lorandszakacs.me"

  lazy val scalaVersion = "2.10.3"

  lazy val scalacOptions = Seq("-deprecation")

  lazy val javaOptions = Seq("-Xmx1G")
}

object Testing { 

  def libraryDependencies = Seq(
    "org.scalatest" % "scalatest_2.10" % "2.1.0" % "test"
    //"org.scalacheck" %% "scalacheck" % "1.11.3" % "test",
    //"org.specs2" %% "specs2" % "2.3.8" % "test"
  )

  //required for use with the specs2 library
  def resolvers =
      Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

  //required for use with the specs2 library.
  //def scalacOptions = Seq("-Yrangepos")
  def scalacOptions = Seq()

  def javaOptions = Seq("-Xmx2G")
}