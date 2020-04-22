lazy val `netflow-stream-lib` = project
  .settings(Settings.common)
  .settings(
    libraryDependencies ++= Seq(
      Deps.nettyBuffer,
      Deps.akkaActor,
      Deps.akkaStream,
    ),
  )

lazy val `stream-util-model` = project
  .settings(Settings.common)
  .settings(
    libraryDependencies ++= Seq(
      Deps.akkaStream,
    ),
  )

lazy val `stream-util` = project
  .settings(Settings.common)
  .settings(
    libraryDependencies ++= Seq(
      Deps.akkaStream,
    ),
    libraryDependencies ++= Seq(
      Deps.akkaTestkit,
      Deps.scalatest,
    ).map(_ % Test),
  )
  .dependsOn(`stream-util-model`)

lazy val `udp-stream-util` = project
  .settings(Settings.common)
  .settings(
    libraryDependencies ++= Seq(
      Deps.akkaStream,
      Deps.alpakkaUdp,
    ),
    libraryDependencies ++= Seq(
      Deps.akkaTestkit,
      Deps.scalatest,
    ).map(_ % Test),
  )
  .dependsOn(`stream-util` % "test->test;compile->compile")

lazy val `netflow-receiver` = project
  .settings(Settings.common)
  .settings(
    libraryDependencies ++= Seq(
      Deps.alpakkaUdp,
    ),
    libraryDependencies ++= Seq(
      Deps.akkaTestkit,
      Deps.scalatest,
    ).map(_ % Test),
  )
  .dependsOn(`stream-util` % "test->test;compile->compile", `udp-stream-util`, `netflow-stream-lib`)
  .aggregate(`stream-util-model`, `stream-util`, `udp-stream-util`, `netflow-stream-lib`)

Global / onLoad += { (s: State) =>
  "project netflow-receiver" :: s
}
