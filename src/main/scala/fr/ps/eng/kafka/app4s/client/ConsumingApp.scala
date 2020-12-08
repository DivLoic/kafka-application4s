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
    // TODO: use reflectionAvroDeserializer4S to create the following serializers
    val keyDeserializer: Deserializer[Key] = ???
    val tvShowDeserializer: Deserializer[TvShow] = ???
    val ratingDeserializer: Deserializer[Rating] = ???

    keyDeserializer.configure(config.deserializerConfig.toMap.asJava, true)
    // TODO: call Serializer#configure on the other serializers

    val baseConfig: Map[String, AnyRef] = config.consumerConfig.toMap

    val consumerConfig1 = baseConfig ++ Map("group.id" -> "group1", "fetch.max.bytes" -> "50") asJava
    val consumerConfig2 = baseConfig ++ Map("group.id" -> "group2", "enable.auto.commit" -> "true") asJava

    // TODO: create an instance of KafkaConsumer named consumer1 based on consumerConfig1
    val consumer1: KafkaConsumer[Key, TvShow] = ???

    // (1) backtracking from the beginning
    val tvShowPartition: Vector[TopicPartition] = consumer1
      .partitionsFor(config.tvShowTopicName)
      .asScala
      .toVector
      .map(info => new TopicPartition(info.topic(), info.partition()))

    // TODO: Assign consumer1 to the partitions contained in tvShowPartition
    // TODO: Set consumer1 to the beginning of the partitions contained in tvShowPartition

    val records: ConsumerRecords[Key, TvShow] = consumer1.poll(config.pollingTimeout.toJava)

    logger info s"Just polled the ${records.count()} th TV shows."
    logger warn s"Closing the the first consumer n°1 now!"
    Try(consumer1.close())
      .recover { case error => logger.error("Failed to close the kafka consumer", error) }

    // (2) consuming the latest messages
    val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    scheduler.schedule(() => {
      var map = Map.empty[String, Int]
      // TODO: create a new ConsumerRebalanceListener
      val listener: ConsumerRebalanceListener = ???

      val consumer2 = new KafkaConsumer[Key, Rating](consumerConfig2, keyDeserializer, ratingDeserializer)
      // TODO: subscribe consumer2 to config.ratingTopicName

      while (!scheduler.isShutdown) {
        Thread.sleep(2000)
        Try {
          // TODO: use consumer2 to poll records (hint: use config.pollingTimeout)
          val records: ConsumerRecords[Key, Rating] = ???
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
      // TODO: close consumer2

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
