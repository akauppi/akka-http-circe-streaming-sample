// build.sbt
//
lazy val defaults = Def.settings(
  organization := "org.akauppi",
  version := "0.0.0-SNAPSHOT",

  scalaVersion := "2.12.6",
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "utf8",
    "-feature",
    "-unchecked",
    //"-Xfatal-warnings",
    //"-Xlint",
    //"-Ywarn-dead-code",
    //"-Ywarn-numeric-widen",
    //"-Ywarn-value-discard",
    //"-Xfuture",
    "-language", "postfixOps"
  )
)

//--- Dependencies ---

lazy val commonDeps = Def.settings(
  libraryDependencies ++= Seq(
    "com.typesafe" % "config" % "1.3.3",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"
  )
)

val akkaVer = "2.5.12"
val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVer
val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaVer
//val akkaStreamTestkit = "com.typesafe.akka" %% "akka-stream-testkit" % akkaVer % Test

val akkaHttpVer = "10.1.1"
val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVer
val akkaHttpTestkit = "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVer % Test

val circeVersion = "0.9.3"
val circeCore = "io.circe" %% "circe-core" % circeVersion
val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
val circeParser = "io.circe" %% "circe-parser" % circeVersion
val circeJava8 = "io.circe" %% "circe-java8" % circeVersion
val circeLiteral = "io.circe" %% "circe-literal" % circeVersion

val akkaHttpCirce = "de.heikoseeberger" %% "akka-http-circe" % "1.20.1"

val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5" % Test

//--- Projects ---

// Note: Project name shows in IntelliJ IDEA
//
val bridge = (project in file(".")).settings(defaults, commonDeps)
  .settings(
    libraryDependencies ++= Seq(
      circeGeneric,
      akkaStream,
      akkaHttp,
      akkaHttpCirce,
      //
      scalaTest,
      akkaHttpTestkit
    )
  )
