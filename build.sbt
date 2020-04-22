import xerial.sbt.Sonatype.GitHubHosting

lazy val `netflow-stream-lib` = project
  .settings(Settings.common)
  .settings(
    libraryDependencies ++= Seq(
      "io.netty" % "netty-buffer" % "4.1.48.Final",
      "com.typesafe.akka" %% "akka-actor" % "2.6.4",
      "com.typesafe.akka" %% "akka-stream" % "2.6.4",
      "com.lightbend.akka" %% "akka-stream-alpakka-udp" % "1.1.2"
    ),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-testkit" % "2.6.4",
      "org.scalatest" %% "scalatest" % "3.1.1"
    ).map(_ % Test)
  )

Global / onLoad += { (s: State) =>
  "project netflow-stream-lib" :: s
}

releaseCrossBuild in ThisBuild := true

releasePublishArtifactsAction in ThisBuild := PgpKeys.publishSigned.value

publishMavenStyle in ThisBuild := true

licenses in ThisBuild := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

sonatypeProjectHosting in ThisBuild := Some(GitHubHosting("CodeMettle", "netflow", "steven@codemettle.com"))

publishTo in ThisBuild := sonatypePublishTo.value
