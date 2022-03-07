import Dependencies._

ThisBuild / scalaVersion     := "2.13.7"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "swam-hello-world",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += "org.gnieh" %% "swam-core" % "0.5.0",
    libraryDependencies += "org.gnieh" %% "swam-runtime" % "0.5.0",
    libraryDependencies += "org.gnieh" %% "swam-text" % "0.5.0",
    libraryDependencies += "org.typelevel" %% "cats-effect" % "2.5.4",
    libraryDependencies += "co.fs2" %% "fs2-core" % "2.0.1",
    libraryDependencies += "co.fs2" %% "fs2-io" % "2.0.1"


  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
