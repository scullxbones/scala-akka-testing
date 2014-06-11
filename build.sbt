import com.typesafe.sbt.SbtMultiJvm
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm

val akkaVersion = "2.3.3"

val project = Project(
  id = "scala-akka-testing",
  base = file("."),
  settings = Project.defaultSettings ++ SbtMultiJvm.multiJvmSettings ++ Seq(
    name := "scala-akka-testing",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.10.0",
    organization := "com.github.scullxbones",
    testOptions in Test ++= Seq(
      Tests.Argument(TestFrameworks.ScalaTest, "-u", "target/test-reports"),
      Tests.Argument(TestFrameworks.Specs2,"junitxml","console","html")
    ),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion, 
      "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
      "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion % "test",
      "org.scalatest" %% "scalatest" % "2.1.7" % "test",
      "org.specs2" %% "specs2" % "2.3.12" % "test",
      "junit" % "junit" % "4.11" % "test",
      "org.scalamock" %% "scalamock-scalatest-support" % "3.1.RC1" % "test",
      "org.mockito" % "mockito-all" % "1.9.5" % "test",
      "org.scalacheck" %% "scalacheck" % "1.11.4" % "test",
      "ch.qos.logback" % "logback-classic" % "1.1.2" % "test"
    ),
    unmanagedSourceDirectories in Compile <<= (scalaSource in Compile)(Seq(_)),
    unmanagedSourceDirectories in Test <<= (scalaSource in Test)(Seq(_)),
    unmanagedSourceDirectories in MultiJvm <<= (scalaSource in MultiJvm)(Seq(_)),
    compile in MultiJvm <<= (compile in MultiJvm) triggeredBy (compile in Test),
    parallelExecution in Test := false,
    executeTests in Test <<= (executeTests in Test, executeTests in MultiJvm) map {
      case (testResults, multiNodeResults)  =>
        val overall =
          if (testResults.overall.id < multiNodeResults.overall.id)
            multiNodeResults.overall
          else
            testResults.overall
        Tests.Output(overall,
          testResults.events ++ multiNodeResults.events,
          testResults.summaries ++ multiNodeResults.summaries)
    }
  )
) configs (MultiJvm)
