import sbt._

object Dependencies {

  lazy val rootDependencies: List[ModuleID] =
    "org.typelevel" %% "cats-core" % "2.1.1" ::
      "ch.qos.logback" % "logback-classic" % "1.2.3" ::
      "com.github.pureconfig" %% "pureconfig" % "0.13.0" ::
      "com.sksamuel.avro4s" %% "avro4s-core" % "3.1.1" ::
      "com.nrinaudo" %% "kantan.csv" % "0.6.1" ::
      "com.nrinaudo" %% "kantan.csv-enumeratum" % "0.6.1" :: Nil

  lazy val kafkaClientsDeps: List[ModuleID] =
    "org.apache.kafka" % "kafka-clients" % "2.6.0" ::
      "io.confluent" % "kafka-avro-serializer" % "6.0.0" :: Nil

  lazy val testDependencies: List[ModuleID] =
    "org.scalatest" %% "scalatest" % "3.2.3" ::
      "org.scalactic" %% "scalactic" % "3.2.3" ::
      "org.scalacheck" %% "scalacheck" % "1.15.1" ::
      "org.typelevel" %% "cats-core" % "2.3.0" :: Nil
}
