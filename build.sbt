name := "StreamingConfusionMatrixCalculator"

version := "0.1"

scalaVersion := "2.13.5"

val AkkaVersion = "2.6.14"


libraryDependencies += "com.typesafe.akka" %% "akka-stream" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion% Test
libraryDependencies += "org.elasticsearch.client" % "elasticsearch-rest-high-level-client" % "7.3.2"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.7" % "test"


