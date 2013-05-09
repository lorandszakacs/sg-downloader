name := "SG-downloader"

version := "0.1"

scalaVersion := "2.10.1"

libraryDependencies +=  "org.apache.httpcomponents" % "httpclient" % "4.2.5"

scalaSource in Compile <<= baseDirectory(_ / "src")

scalaSource in Test <<= baseDirectory(_ / "test")

// set the main class for packaging the main jar
// 'run' will still auto-detect and prompt
// change Compile to Test to set it for the test jar
mainClass in (Compile, packageBin) := Some("home.sg.main.Main")

// set the main class for the main 'run' task
// change Compile to Test to set it for 'test:run'
mainClass in (Compile, run) := Some("home.sg.main.Main")

// disable printing timing information, but still print [success]
showTiming := true

// disable printing a message indicating the success or failure of running a task
showSuccess := true

// Execute tests in the current project serially
// Tests from other projects may still run concurrently.
parallelExecution in Test := false