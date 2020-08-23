package fr.ps.eng.kafka.app4s.common

import com.sksamuel.avro4s.RecordFormat
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import org.apache.avro.generic.IndexedRecord
import org.apache.kafka.common.serialization.{Deserializer, Serializer}
/**
 * Created by loicmdivad.
 */
trait HelperSerdes {

    implicit lazy val keyFormat: RecordFormat[Key] = RecordFormat[Key]
    implicit lazy val tvShowFormat: RecordFormat[TvShow] = RecordFormat[TvShow]
    implicit lazy val ratingFormat: RecordFormat[Rating] = RecordFormat[Rating]

    def reflectionAvroSerializer4S[T: RecordFormat]: Serializer[T] = new Serializer[T] {
      val inner = new KafkaAvroSerializer()

      override def configure(configs: java.util.Map[String, _], isKey: Boolean): Unit =
        inner.configure(configs, isKey)

      override def serialize(topic: String, maybeData: T): Array[Byte] = Option(maybeData)
        .map(data => inner.serialize(topic, implicitly[RecordFormat[T]].to(data)))
        .getOrElse(Array.emptyByteArray)

      override def close(): Unit = inner.close()
    }

    def reflectionAvroDeserializer4S[T: RecordFormat]: Deserializer[T] = new Deserializer[T] {
      val inner = new KafkaAvroDeserializer()

      override def configure(configs: java.util.Map[String, _], isKey: Boolean): Unit =
        inner.configure(configs, isKey)

      override def deserialize(topic: String, maybeData: Array[Byte]): T = Option(maybeData)
        .filter(_.nonEmpty)
        .map { data =>
          implicitly[RecordFormat[T]]
            .from(inner.deserialize(topic, data).asInstanceOf[IndexedRecord])
        }
        .getOrElse(null.asInstanceOf[T])

      override def close(): Unit = inner.close()
    }
}
