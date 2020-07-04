
ThisBuild / scalaVersion     := "2.13.3"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "fr.ps.eng"
ThisBuild / organizationName := "ps-engineering"

lazy val root = (project in file(".")).settings(
    name := "kafka-application4s",
    libraryDependencies ++= Dependencies.coreDependencies
)

lazy val clients = (project in file("part1-kafka-clients")).settings(
    name := "part1-kafka-clients",
    libraryDependencies ++= Dependencies.kafkaClientsDeps
)

lazy val streaming = (project in file("part2-kafka-streams")).settings(
    name := "part2-kafka-streams",
    libraryDependencies ++= Dependencies.kafkaStreamsDeps
)

lazy val contrib = (project in file("part3-third-party")).settings(
  name := "part3-third-party",
  libraryDependencies ++= Dependencies.kafkaContribDeps
)
