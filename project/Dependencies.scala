import sbt._

object Dependencies {

  lazy val rootDependencies: List[ModuleID] =
    "com.github.pureconfig" %% "pureconfig" % "0.13.0" ::
      "org.scalatest" %% "scalatest" % "3.1.1" % Test :: Nil

  lazy val kafkaClientsDeps: List[ModuleID] =
    "org.apache.kafka" % "kafka-clients" % "2.5.0" :: Nil

  lazy val kafkaStreamsDeps: List[ModuleID] =
    "org.apache.kafka" %% "kafka-streams-scala" % "2.5.0" ::
      "org.apache.kafka" % "kafka-streams-test-utils" % "2.5.0" % Test :: Nil

  lazy val kafkaContribDeps: List[ModuleID] =
    "com.typesafe.akka" %% "akka-stream-kafka" % "2.0.3" ::
      "com.github.fd4s" %% "fs2-kafka" % "1.0.0" :: Nil
}
