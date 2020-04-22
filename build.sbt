import xerial.sbt.Sonatype.GitHubHosting

lazy val `stream-util-model` = project
  .settings(Settings.common)
  .settings(
    libraryDependencies ++= Seq(
      Deps.akkaStream,
    ),
  )

lazy val `netflow-stream-lib` = project
  .settings(Settings.common)
  .settings(
    libraryDependencies ++= Seq(
      Deps.nettyBuffer,
      Deps.akkaActor,
      Deps.akkaStream,
      Deps.alpakkaUdp,
    ),
    libraryDependencies ++= Seq(
      Deps.akkaTestkit,
      Deps.scalatest,
    ).map(_ % Test),
  )
  .dependsOn(`stream-util-model`)
  .aggregate(`stream-util-model`)

Global / onLoad += { (s: State) =>
  "project netflow-stream-lib" :: s
}

releaseCrossBuild in ThisBuild := true

releasePublishArtifactsAction in ThisBuild := PgpKeys.publishSigned.value

publishMavenStyle in ThisBuild := true

licenses in ThisBuild := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

sonatypeProjectHosting in ThisBuild := Some(GitHubHosting("CodeMettle", "netflow", "steven@codemettle.com"))

publishTo in ThisBuild := sonatypePublishTo.value
