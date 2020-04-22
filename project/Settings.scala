import sbt.Keys._
import sbt._

object Settings {
  final val common = Seq[Setting[_]](
    organization := "com.codemettle",
    crossScalaVersions := Seq("2.13.1", "2.12.11"),
    scalaVersion := crossScalaVersions.value.head,
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit", "-encoding", "utf8", "-feature"),
    javacOptions ++= Seq("-Xlint:deprecation")
  )
}
