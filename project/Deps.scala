import sbt._

object Deps {
  final val akkaActor = "com.typesafe.akka" %% "akka-actor" % Versions.akka
  final val akkaStream = "com.typesafe.akka" %% "akka-stream" % Versions.akka
  final val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % Versions.akka
  final val alpakkaUdp = "com.lightbend.akka" %% "akka-stream-alpakka-udp" % Versions.alpakka
  final val nettyBuffer = "io.netty" % "netty-buffer" % Versions.netty
  final val scalatest = "org.scalatest" %% "scalatest" % Versions.scalatest
}
