
ThisBuild / scalaVersion     := "2.13.3"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "fr.ps.eng"
ThisBuild / organizationName := "ps-engineering"
ThisBuild / javacOptions     ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")
ThisBuild / scalacOptions    ++= Seq("-language:postfixOps")

lazy val root = (project in file("."))
  .settings(
    name := "kafka-application4s",
    libraryDependencies ++= Dependencies.rootDependencies,
    resolvers += "Confluent Repo" at "http://packages.confluent.io/maven"
  )

lazy val clients = (project in file("part1-kafka-clients"))
  .dependsOn(root)
  .settings(
    name := "part1-kafka-clients",
    libraryDependencies ++= Dependencies.kafkaClientsDeps
  )

lazy val streaming = (project in file("part2-kafka-streams"))
  .dependsOn(root)
  .settings(
    name := "part2-kafka-streams",
    libraryDependencies ++= Dependencies.kafkaStreamsDeps
  )

lazy val contrib = (project in file("part3-third-party"))
  .dependsOn(root)
  .settings(
    name := "part3-third-party",
    libraryDependencies ++= Dependencies.kafkaContribDeps
  )
