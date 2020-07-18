package fr.ps.eng.kafka.app4s.part1

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
}
