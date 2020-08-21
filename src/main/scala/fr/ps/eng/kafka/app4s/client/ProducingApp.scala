package fr.ps.eng.kafka.app4s.client

import cats.syntax.either._

import java.time.Instant
import java.util.TimerTask
import java.util.UUID.randomUUID
import java.util.concurrent.Future

import fr.ps.eng.kafka.app4s.client.Conf.ProducerAppConfig
import fr.ps.eng.kafka.app4s.common._
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.header.Header
import org.apache.kafka.common.header.internals.{RecordHeader, RecordHeaders}
import org.apache.kafka.common.serialization.Serializer
import org.slf4j.{Logger, LoggerFactory}
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import scala.util.{Random, Success, Try}

/**
 * Created by loicmdivad.
 */
object ProducingApp extends App with HelperFunctions with HelperSerdes {

  lazy val logger: Logger = LoggerFactory.getLogger(getClass)

  val configFile = sys.props.get("config.file").getOrElse(getClass.getResource("/producer.conf").getPath)

  ConfigSource.file(configFile).load[ProducerAppConfig].map { config =>
    // (0) configure serializers and producers
    val keySerializer: Serializer[Key] = reflectionAvroSerializer4S[Key]
    val tvShowSerializer: Serializer[TvShow] = reflectionAvroSerializer4S[TvShow]
    val ratingSerializer: Serializer[Rating] = reflectionAvroSerializer4S[Rating]

    keySerializer.configure(config.serializerConfig.toMap.asJava, true)
    tvShowSerializer :: ratingSerializer :: Nil foreach (_.configure(config.serializerConfig.toMap.asJava, false))

    val baseConfig: Map[String, AnyRef] = config.producerConfig.toMap

    val producerConfig1 = baseConfig ++ Map("client.id" -> "client1", "linger.ms" -> s"${(1 minute) toMillis}") asJava
    val producer1 = new KafkaProducer[Key, TvShow](producerConfig1, keySerializer, tvShowSerializer)

    val producerConfig2 = baseConfig ++ Map("client.id" -> "client2", "retries" -> "0") asJava
    val producer2 = new KafkaProducer[Key, Rating](producerConfig2, keySerializer, ratingSerializer)

    val producerConfig3 = baseConfig ++ Map("client.id" -> "client3", "transactional.id" -> "client3") asJava
    val producer3 = new KafkaProducer[Key, Rating](producerConfig3, keySerializer, ratingSerializer)

    // (1) batching the complete tv show collection
    logger info "Batching the tv show referential dataset now ..."
    val _: Vector[Future[RecordMetadata]] = Dataset.AllTvShows.toVector.map { case (showKey, showValue) =>
      val record = new ProducerRecord[Key, TvShow](config.tvShowTopicName, showKey, showValue)
      producer1 send record
    }

    Try {
      producer1.flush()
      producer1.close()
      logger info "Successfully produce the complete TV show collection."
      logger info s"${Dataset.AllTvShows size} TV shows from the " +
        "Netflix, Hulu and Disney+'s catalogs are available to consumers."
    }.recover {
      case error: InterruptedException => logger.error("failed to flush and close the producer", error)
      case error => logger.error("An unexpected error occurs while producing the show collection", error)
    }

    // (2) captiously send new records
    val generator = new java.util.Timer()
    val tasks: Seq[TimerTask] = (0 until config.generatorParallelismLevel) map { genId =>
      val userId = s"user-${randomUUID().toString.take(8)}"
      new java.util.TimerTask {
        override def run(): Unit = {
          val rating: Short = Random.nextInt(5).toShort
          val eventTime: Long = Instant.now.toEpochMilli
          val (showKey, showValue) = getRandomTvShow(Dataset.AllTvShows)
          val genHeader = new RecordHeader("generator-id", s"GENERATOR-$genId" getBytes)
          val showHeader =
            new RecordHeader("details", s"Rating for ${showValue name} (on ${showValue platform})" getBytes)

          val randomRecord: ProducerRecord[Key, Rating] = new ProducerRecord[Key, Rating](
            config.ratingTopicName,
            null, // let the defaultPartitioner do its job
            eventTime,
            showKey,
            Rating(userId, rating),
            new RecordHeaders(Iterable[Header](genHeader, showHeader) asJava)
          )

          producer2 send randomRecord
        }
      }
    }

    logger info s"Starting the rating generator with ${tasks.length} threads."
    tasks foreach (generator.schedule(_, randomDelay(2 second), randomPeriod(config.generatorPeriod)))

    // (3) open, perform and close a transaction
    val producerCallback: Callback = new Callback {
      override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = Option(exception)

        .map(error => logger.error("fail to produce a record due to: ", error))

        .getOrElse(logger info s"Successfully produce a new record to kafka: ${
          s"topic: ${metadata.topic()}, partition: ${metadata.partition()}, offset: ${metadata.offset()}"
        }")
    }

    producer3.initTransactions()
    var exitFlag: Either[String, Try[_]] = Right(Success())

    sys.addShutdownHook {
      logger info "Stopping all the generator threads ..."
      generator.cancel()
      Try {
        producer2 :: producer3 :: Nil foreach { producer =>
          producer.flush()
          producer.close()
        }
      }
      logger info "Closing the producer app now!"
    }

    while (!exitFlag.swap.contains("exit")) {
      logger info s"Asking for 3 ratings to perform a transaction:"

      exitFlag = for {
        record1 <- getConsoleRating(config)
        record2 <- getConsoleRating(config)
        record3 <- getConsoleRating(config)
      } yield {
        Try {
          producer3.beginTransaction()
          producer3.send(record1, producerCallback)
          producer3.send(record2, producerCallback)
          producer3.send(record3, producerCallback)
          producer3.commitTransaction()
          logger info "A transaction of 3 records has been completed."
        }.recover {
          case _ =>
            logger error "A failure occurs during the transaction."
            producer3.abortTransaction();
        }
      }

      exitFlag
        .left
        .filterToOption(_ != "exit")
        .foreach { input =>
          logger warn s"$input was unexpected: type [0-5] or 'pass' or 'exit'."
        }

    }

  }.recover {
    case failures =>

      logger error "Failed to parse the configuration of the producer application."

      failures.toList.foreach(err => logger.error(err.description))

      sys.exit(-1)
  }

  def randomDelay(max: FiniteDuration): Long = Math.abs(Random.nextLong(max toMillis))

  def randomPeriod(max: FiniteDuration): Long = Math.abs(max toMillis)

  def getRandomTvShow(tvShowMap: Map[Key, TvShow]): (Key, TvShow) =
    tvShowMap.toList(Random.nextInt(tvShowMap.size))

  def getConsoleRating(config: ProducerAppConfig): Either[String, ProducerRecord[Key, Rating]] = {
    def wrap(key: Key, short: Short) =
      new ProducerRecord(config.ratingTopicName, key, Rating("console", short))

    var input = "pass"
    var showId: Key = null
    var show: TvShow = null
    while (input equals "pass") {
      val pair = getRandomTvShow(Dataset.`200TvShows`)
      showId = pair._1
      show = pair._2
      print(s"""Have you watched "${show name}" on ${show platform}? how was it (from 0 to 5)?\n> """)
      input = scala.io.StdIn.readLine()
    }

    Try(input.toShort)
      .filter(_ <= 5)
      .map(wrap(showId, _))
      .toEither
      .left
      .map(_ => input)
  }
}
