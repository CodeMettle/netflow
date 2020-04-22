import com.jsuereth.sbtpgp.SbtPgp.autoImport.PgpKeys
import sbt.Keys._
import sbt._
import sbtrelease.ReleasePlugin.autoImport.{releaseCrossBuild, releasePublishArtifactsAction}
import xerial.sbt.Sonatype.GitHubHosting
import xerial.sbt.Sonatype.autoImport.{sonatypeProjectHosting, sonatypePublishTo}

object Settings {
  final val common = Seq[Setting[_]](
    organization := "com.codemettle",
    crossScalaVersions := Versions.scala,
    scalaVersion := crossScalaVersions.value.head,
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit", "-encoding", "utf8", "-feature"),
    javacOptions ++= Seq("-Xlint:deprecation"),
    releaseCrossBuild := true,
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    publishMavenStyle := true,
    licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    sonatypeProjectHosting := Some(GitHubHosting("CodeMettle", "netflow", "steven@codemettle.com")),
    publishTo := sonatypePublishTo.value
  )
}
