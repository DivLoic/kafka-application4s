package fr.ps.eng.kafka.app4s.client

import com.typesafe.config.Config

import scala.concurrent.duration.FiniteDuration

/**
 * Created by loicmdivad.
 */
object Conf {

  case class ProducerAppConfig(producerConfig: Config,
                               serializerConfig: Config,
                               tvShowTopicName: String,
                               ratingTopicName: String,
                               generatorPeriod: FiniteDuration,
                               generatorParallelismLevel: Int = 1)

  case class ConsumerAppConfig(consumerConfig: Config,
                               deserializerConfig: Config,
                               tvShowTopicName: String,
                               ratingTopicName: String,
                               pollingTimeout: FiniteDuration)
}
