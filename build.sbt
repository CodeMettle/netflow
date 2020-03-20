name := "netflow-stream-lib"

organization := "io.wasted"

version := scala.io.Source.fromFile("version").mkString.trim

scalaVersion := "2.13.1"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit", "-encoding", "utf8", "-feature")

scalacOptions ++= Seq("-language:higherKinds",
                      "-language:postfixOps",
                      "-language:implicitConversions",
                      "-language:reflectiveCalls",
                      "-language:existentials")

javacOptions ++= Seq("-Xlint:deprecation")

libraryDependencies ++= {
  Seq(
    "io.netty" % "netty-buffer" % "4.1.48.Final",
    "com.typesafe.akka" %% "akka-actor" % "2.6.4",
    "com.typesafe.akka" %% "akka-stream" % "2.6.4",
    "com.lightbend.akka" %% "akka-stream-alpakka-udp" % "1.1.2"
  )
}

publishMavenStyle := true
credentials += {
  def file = "credentials-" + (if (isSnapshot.value) "snapshots" else "internal")

  Credentials(Path.userHome / ".m2" / file)
}
publishTo := {
  def path = "/repository/" + (if (isSnapshot.value) "snapshots" else "internal")

  Some("CodeMettle Maven" at s"http://maven.codemettle.com$path")
}

buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion) ++ Seq[BuildInfoKey](
  "commit" -> ("git rev-parse HEAD" !!).trim
)

buildInfoPackage := "io.netflow.lib"

enablePlugins(BuildInfoPlugin)
