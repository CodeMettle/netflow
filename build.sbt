/*
import scalariform.formatter.preferences._
*/

name := "netflow-lib"

organization := "io.wasted"

version := scala.io.Source.fromFile("version").mkString.trim

scalaVersion := "2.13.1" /*"2.11.6"*/

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit", "-encoding", "utf8", "-feature")

scalacOptions ++= Seq("-language:higherKinds", "-language:postfixOps", "-language:implicitConversions", "-language:reflectiveCalls", "-language:existentials")

javacOptions ++= Seq(/*"-target", "1.7", "-source", "1.7", */"-Xlint:deprecation")

//mainClass in assembly := Some("io.netflow.Node")

libraryDependencies ++= {
  Seq(
    "io.netty" % "netty-all" % "4.1.22.Final",
    "com.typesafe.akka" %% "akka-actor" % "2.6.4"
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
  "commit" -> ("git rev-parse HEAD"!!).trim
)

buildInfoPackage := "io.netflow.lib"

enablePlugins(BuildInfoPlugin)

/*
addArtifact(Artifact("netflow", "server"), assembly)

resolvers ++= Seq(
  "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository",
  "wasted.io/repo" at "http://repo.wasted.io/mvn",
  "Websudos releases" at "http://maven.websudos.co.uk/ext-release-local",
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Twitter's Repository" at "http://maven.twttr.com/",
  "Typesafe Ivy Repo" at "http://repo.typesafe.com/typesafe/ivy-releases",
  "Java.net Maven2 Repository" at "http://download.java.net/maven/2/"
)
*/
