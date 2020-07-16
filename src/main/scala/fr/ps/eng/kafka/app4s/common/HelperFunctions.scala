package fr.ps.eng.kafka.app4s.common

import java.util.Properties

import com.typesafe.config.Config

import scala.jdk.CollectionConverters._

/**
 * Created by loicmdivad.
 */
trait HelperFunctions {

  implicit class configMapperOps(config: Config) {

    def toMap: Map[String, AnyRef] = config
      .entrySet()
      .asScala
      .map(pair => (pair.getKey, config.getAnyRef(pair.getKey)))
      .toMap

    def toProperties: Properties = {
      val properties = new Properties()
      properties.putAll(config.toMap.asJava)
      properties
    }
  }

}
