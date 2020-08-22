package fr.ps.eng.kafka.app4s.client

import cats.instances.map._
import cats.instances.int._
import cats.syntax.either._
import cats.syntax.monoid._

import java.util
import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}

import fr.ps.eng.kafka.app4s.client.Conf.ConsumerAppConfig
import fr.ps.eng.kafka.app4s.common._
import org.apache.kafka.clients.consumer.{ConsumerRebalanceListener, ConsumerRecords, KafkaConsumer}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.Deserializer
import org.slf4j.{Logger, LoggerFactory}
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import scala.jdk.CollectionConverters._
import scala.jdk.DurationConverters._
import scala.util.Try

/**
 * Created by loicmdivad.
 */
object ConsumingApp extends App with HelperFunctions with HelperSerdes {

  lazy val logger: Logger = LoggerFactory.getLogger(getClass)

  val configFile = sys.props.get("config.file").getOrElse(getClass.getResource("/consumer.conf").getPath)

  ConfigSource.file(configFile).load[ConsumerAppConfig].map { config =>
    // (0) configure serializers and producers
    val keyDeserializer: Deserializer[Key] = reflectionAvroDeserializer4S[Key]
    val tvShowDeserializer: Deserializer[TvShow] = reflectionAvroDeserializer4S[TvShow]
    val ratingDeserializer: Deserializer[Rating] = reflectionAvroDeserializer4S[Rating]

    keyDeserializer.configure(config.deserializerConfig.toMap.asJava, true)
    tvShowDeserializer :: ratingDeserializer :: Nil foreach (_.configure(config.deserializerConfig.toMap.asJava, false))

    val baseConfig: Map[String, AnyRef] = config.consumerConfig.toMap

    val consumerConfig1 = baseConfig ++ Map("group.id" -> "group1", "fetch.max.bytes" -> "50") asJava
    val consumer1 = new KafkaConsumer[Key, TvShow](consumerConfig1, keyDeserializer, tvShowDeserializer)

    val consumerConfig2 = baseConfig ++ Map("group.id" -> "group2", "enable.auto.commit" -> "true") asJava

    // (1) backtracking from the beginning
    val tvShowPartition: Vector[TopicPartition] = consumer1
      .partitionsFor(config.tvShowTopicName)
      .asScala
      .toVector
      .map(info => new TopicPartition(info.topic(), info.partition()))

    consumer1.assign(tvShowPartition asJava)
    consumer1.seekToBeginning(consumer1.assignment())

    val records: ConsumerRecords[Key, TvShow] = consumer1.poll(config.pollingTimeout.toJava)

    logger info s"Just polled the ${records.count()} th TV shows."
    logger warn s"Closing the the first consumer n°1 now!"
    Try(consumer1.close())
      .recover { case error => logger.error("Failed to close the kafka consumer", error) }

    // (2) consuming the latest messages
    val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    scheduler.schedule(() => {
      var map = Map.empty[String, Int]
      val listener = new ConsumerRebalanceListener {
        override def onPartitionsRevoked(partitions: util.Collection[TopicPartition]): Unit =
          logger info s"The following partition are revoked: ${partitions.asScala.mkString(", ")}"

        override def onPartitionsAssigned(partitions: util.Collection[TopicPartition]): Unit =
          logger info s"The following partition are assigned: ${partitions.asScala.mkString(", ")}"
      }

      val consumer2 = new KafkaConsumer[Key, Rating](consumerConfig2, keyDeserializer, ratingDeserializer)
      consumer2.subscribe(config.ratingTopicName :: Nil asJava, listener)

      while (!scheduler.isShutdown) {
        Thread.sleep(2000)
        Try {
          val records = consumer2.poll(config.pollingTimeout.toJava)
          records.iterator().asScala.toVector
        }.map {
          _.groupBy(_.value().user)
            .view
            .mapValues(_.size)
            .toMap

        }.recover { case error =>
          logger.error("something wrong happened", error)
          Map.empty[String, Int]

        }.foreach { recordMap =>
          map = map |+| recordMap
          if(map.nonEmpty) print(s"\rPolled ${printRating(map)}.")
        }
      }

      println()
      logger warn s"Closing the the first consumer n°2 now!"
      Try(consumer2.close())
        .recover { case error => logger.error("Failed to close the kafka consumer", error) }

    }, 1, TimeUnit.SECONDS)

    sys.addShutdownHook {
      scheduler.shutdown()
      scheduler.awaitTermination(10, TimeUnit.SECONDS)
    }

  }.recover {
    case failures =>

      logger error "Failed to parse the configuration of the consumer application."

      failures.toList.foreach(err => logger.error(err.description))

      sys.exit(-1)
  }

  def printRating(map: Map[String, Int]): String = map
    .map { case (user, events) => s"$user: $events ⭐️" }
    .toList
    .sorted
    .mkString(", ")
}
