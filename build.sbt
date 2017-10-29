import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "nl.oakhill",
      scalaVersion := "2.12.3",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "Hashtools",
    libraryDependencies ++= Seq(scalaTest % Test,
      scalaMock % Test,
      "com.jsuereth" %% "scala-arm" % "2.0",
      "io.monix" %% "monix" % "2.3.0",
      "io.monix" %% "monix-cats" % "2.3.0",
      "com.monovore" %% "decline" % "0.4.0-M2",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
      "ch.qos.logback" % "logback-classic" % "1.2.3"
      )
  )
