
version := "0.1.0-SNAPSHOT"

organization := "com.github.scullxbones"

lazy val akkaVersion = "2.3.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion, 
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion, 
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion % "test",
  "org.scalatest" %% "scalatest" % "2.1.7" % "test",
  "org.specs2" %% "specs2" % "2.3.12" % "test",
  "junit" % "junit" % "4.11" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.1.RC1" % "test",
  "org.mockito" % "mockito-all" % "1.9.5" % "test",
  "org.scalacheck" %% "scalacheck" % "1.11.4" % "test"
)
