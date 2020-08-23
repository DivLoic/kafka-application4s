import sbt.fullRunTask

ThisBuild / scalaVersion     := "2.13.3"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "fr.ps.eng"
ThisBuild / organizationName := "ps-engineering"
ThisBuild / javacOptions     ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")
ThisBuild / scalacOptions    ++= Seq("-language:postfixOps")

lazy val produce: TaskKey[Unit] = taskKey[Unit]("Message Production")
lazy val consume: TaskKey[Unit] = taskKey[Unit]("Message Consumption")

lazy val root = (project in file("."))
  .settings(
    name := "kafka-application4s",
    resolvers += "Confluent Repo" at "https://packages.confluent.io/maven",
    libraryDependencies ++= (Dependencies.rootDependencies ++ Dependencies.kafkaClientsDeps),
    libraryDependencies ++= (Dependencies.testDependencies map(_ % Test)),
    fullRunTask(produce, Compile, s"fr.ps.eng.kafka.app4s.client.ProducingApp"),
    fullRunTask(consume, Compile, s"fr.ps.eng.kafka.app4s.client.ConsumingApp")
  )
