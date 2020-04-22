import xerial.sbt.Sonatype.GitHubHosting

name := "netflow-stream-lib"

organization := "com.codemettle"

crossScalaVersions := Seq("2.13.1", "2.12.11")

scalaVersion := crossScalaVersions.value.head

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit", "-encoding", "utf8", "-feature")

javacOptions ++= Seq("-Xlint:deprecation")

libraryDependencies ++= {
  Seq(
    "io.netty" % "netty-buffer" % "4.1.48.Final",
    "com.typesafe.akka" %% "akka-actor" % "2.6.4",
    "com.typesafe.akka" %% "akka-stream" % "2.6.4",
    "com.lightbend.akka" %% "akka-stream-alpakka-udp" % "1.1.2"
  )
}

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-testkit" % "2.6.4",
  "org.scalatest" %% "scalatest" % "3.1.1"
).map(_ % Test)

releaseCrossBuild := true

releasePublishArtifactsAction := PgpKeys.publishSigned.value

publishMavenStyle := true

licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

sonatypeProjectHosting := Some(GitHubHosting("CodeMettle", "netflow", "steven@codemettle.com"))

publishTo := sonatypePublishTo.value
