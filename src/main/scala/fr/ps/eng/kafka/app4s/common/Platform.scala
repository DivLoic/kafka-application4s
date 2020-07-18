package fr.ps.eng.kafka.app4s.common

import enumeratum._

/**
 * Created by loicmdivad.
 */
sealed trait Platform extends EnumEntry

object Platform extends Enum[Platform] {

  case object Netflix extends Platform
  case object Hulu extends Platform
  case object PrimeVideo extends Platform
  case object DisneyPlus extends Platform

  override def values: IndexedSeq[Platform] = Vector(Netflix, Hulu, PrimeVideo, DisneyPlus)
}
