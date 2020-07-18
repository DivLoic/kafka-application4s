package fr.ps.eng.kafka.app4s.common

import com.sksamuel.avro4s.AvroName

/**
 * Created by loicmdivad.
 */
case class Key(@AvroName("show_id") showId: String)
