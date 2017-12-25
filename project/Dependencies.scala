import sbt._

object Dependencies {

  lazy val `scala_2.12`:     String = "2.12.4"
  lazy val mainScalaVersion: String = `scala_2.12`

  //============================================================================================
  //========================================== Misc ============================================
  //============================================================================================

  lazy val scalaParserCombinators
    : ModuleID = "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.6" withSources ()

  lazy val nScalaJodaTime: ModuleID = "com.github.nscala-time" %% "nscala-time"   % "2.16.0" withSources ()
  lazy val reactiveMongo:  ModuleID = "org.reactivemongo"      %% "reactivemongo" % "0.12.5" withSources ()
  lazy val typeSafeConfig: ModuleID = "com.typesafe"           % "config"         % "1.3.1" withSources ()

  lazy val scalaLogging:   ModuleID = "com.typesafe.scala-logging" %% "scala-logging"  % "3.5.0" withSources ()
  lazy val logbackClassic: ModuleID = "ch.qos.logback"             % "logback-classic" % "1.1.7" withSources ()

  lazy val pprint: ModuleID = "com.lihaoyi" %% "pprint" % "0.4.3" withSources ()

  object java {
    def jsoup: ModuleID = "org.jsoup" % "jsoup" % "1.8.1" withSources ()
  }
  //============================================================================================
  //================================= http://typelevel.org/scala/ ==============================
  //========================================  typelevel ========================================
  //============================================================================================

  lazy val shapeless: ModuleID = "com.chuusai" %% "shapeless" % "2.3.2" withSources ()

  lazy val catsVersion: String = "1.0.0-RC2"

  lazy val catsCore:    ModuleID = "org.typelevel" %% "cats-core"    % catsVersion withSources ()
  lazy val catsMacros:  ModuleID = "org.typelevel" %% "cats-macros"  % catsVersion withSources ()
  lazy val catsKernel:  ModuleID = "org.typelevel" %% "cats-kernel"  % catsVersion withSources ()
  lazy val catsLaws:    ModuleID = "org.typelevel" %% "cats-laws"    % catsVersion withSources ()
  lazy val catsTestkit: ModuleID = "org.typelevel" %% "cats-testkit" % catsVersion withSources ()

  lazy val catsEffects: ModuleID = "org.typelevel" %% "cats-effect" % "0.6" withSources ()

  lazy val attoParser: ModuleID = "org.tpolecat" %% "atto-core" % "0.6.1-M7" withSources ()

  //============================================================================================
  //================================= http://akka.io/docs/ =====================================
  //======================================== akka ==============================================
  //============================================================================================

  lazy val akkaVersion: String = "2.5.8"

  lazy val akkaActor:           ModuleID = "com.typesafe.akka" %% "akka-actor"            % akkaVersion withSources ()
  lazy val akkaStream:          ModuleID = "com.typesafe.akka" %% "akka-stream"           % akkaVersion withSources ()
  lazy val akkaCluster:         ModuleID = "com.typesafe.akka" %% "akka-cluster"          % akkaVersion withSources ()
  lazy val akkaClusterSharding: ModuleID = "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion withSources ()
  lazy val akkaDistributedData: ModuleID = "com.typesafe.akka" %% "akka-distributed-data" % akkaVersion withSources ()
  lazy val akkaPersistence:     ModuleID = "com.typesafe.akka" %% "akka-persistence"      % akkaVersion withSources ()

  lazy val akkaHttpVersion: String   = "10.0.11"
  lazy val akkaHttp:        ModuleID = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion withSources ()

  //============================================================================================
  //=========================================  testing =========================================
  //============================================================================================

  lazy val scalaTest:  ModuleID = "org.scalatest"  %% "scalatest"  % "3.0.4"  % Test withSources ()
  lazy val scalaCheck: ModuleID = "org.scalacheck" %% "scalacheck" % "1.13.4" % Test withSources ()

  lazy val akkaTestKit: ModuleID = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test withSources ()
  lazy val akkaStreamTestKit
    : ModuleID = "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test withSources ()
  lazy val akkaHttpTestKit
    : ModuleID = "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test withSources ()

}
