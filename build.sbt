import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "nl.oakhill",
      scalaVersion := "2.12.4",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "Hashtools",
    scalastyleConfig := file("project/scalastyle_config.xml"),
    libraryDependencies ++= Seq(scalaTest % Test,
      scalaMock % Test,
      "io.spray" %% "spray-json" % "1.3.4",
      "com.jsuereth" %% "scala-arm" % "2.0",
      "io.monix" %% "monix" % "2.3.2",
      "io.monix" %% "monix-cats" % "2.3.2",
      "com.monovore" %% "decline" % "0.4.0-RC1",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
      "ch.qos.logback" % "logback-classic" % "1.2.3"
      )
  )

lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")

compileScalastyle := scalastyle.in(Compile).toTask("").value

(compile in Compile) := ((compile in Compile) dependsOn compileScalastyle).value
