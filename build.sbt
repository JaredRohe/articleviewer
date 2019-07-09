scalaVersion := "2.13.0"

name := "article-viewer"
organization := "rohe.jared"
version := "1.0"

libraryDependencies += "com.softwaremill.sttp" %% "async-http-client-backend-future" % "1.6.0"
libraryDependencies += "com.softwaremill.sttp" %% "json4s" % "1.6.0"
libraryDependencies += "org.json4s" %% "json4s-native" % "3.6.7"
libraryDependencies ++= Seq("org.slf4j" % "slf4j-api" % "1.7.5",
  "org.slf4j" % "slf4j-simple" % "1.7.5")

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"



assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
