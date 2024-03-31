ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.13"

resolvers += Resolver.ApacheMavenStagingRepo

val pekkoVersion = "1.0.2"
val pekkoHttpVersion = "1.0.1"

lazy val root = (project in file("."))
  .settings(
    name := "pekko-http-sample",
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-actor" % pekkoVersion,
      "org.apache.pekko" %% "pekko-stream" % pekkoVersion,
      "org.apache.pekko" %% "pekko-slf4j" % pekkoVersion,
      "org.apache.pekko" %% "pekko-http" % pekkoHttpVersion,
      "org.slf4j" % "slf4j-simple" % "2.0.12"
    )
  )

