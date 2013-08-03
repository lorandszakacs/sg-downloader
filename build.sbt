import com.typesafe.sbt.SbtStartScript

seq(SbtStartScript.startScriptForClassesSettings: _*)

name := "sg-downloader"

version := "0.1"

scalaVersion := "2.10.2"


//you can get these from maven by creating a string:
//groupID % artifactID % revision

libraryDependencies ++= Seq( 
    "org.apache.httpcomponents" % "httpclient" % "4.2.5",
    "commons-io" % "commons-io" % "2.4",
    "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test",
    "com.novocode" % "junit-interface" % "0.8" % "test->default",
    "com.typesafe" % "config" % "1.0.0",
    "com.typesafe" %% "scalalogging-slf4j" % "1.0.1"
)

scalaSource in Compile <<= baseDirectory(_ / "src/main/scala")

scalaSource in Test <<= baseDirectory(_ / "src/test/scala")

// set the main class for packaging the main jar
// 'run' will still auto-detect and prompt
// change Compile to Test to set it for the test jar
mainClass in (Compile, packageBin) := Some("home.sg.app.Main")

// set the main class for the main 'run' task
// change Compile to Test to set it for 'test:run'
mainClass in (Compile, run) := Some("home.sg.app.Main")

// disable printing timing information, but still print [success]
showTiming := true

// disable printing a message indicating the success or failure of running a task
showSuccess := true

// Execute tests in the current project serially
// Tests from other projects may still run concurrently.
parallelExecution in Test := false

//memory
javaOptions += "-Xmx1G"

//scala compiler options
scalacOptions += "-deprecation"

// append several options to the list of options passed to the Java compiler
javacOptions ++= Seq("-source", "1.6", "-target", "1.6")

// specifies that all tests will be executed in a single external JVM.
fork in Test := true

// define the statements initially evaluated when entering 'console', 'console-quick', or 'console-project'
initialCommands := """
  import home.sg.util._
  import home.sg.parser._
  import home.sg.client._
"""

// set the location of the JDK to use for compiling Java code.
// if 'fork' is true, this is used for 'run' as well
javaHome := Some(file("/Library/Java/JavaVirtualMachines/jdk1.7.0_21.jdk/Contents/Home/"))

// Use Scala from a directory on the filesystem instead of retrieving from a repository
//scalaHome := Some(file("/home/user/scala/trunk/"))

//eclipse project settings:
EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

EclipseKeys.executionEnvironment := Some(EclipseExecutionEnvironment.JavaSE17)

EclipseKeys.projectFlavor := EclipseProjectFlavor.Scala