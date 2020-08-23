package fr.ps.eng.kafka.app4s

import java.time.{LocalDate, ZoneId}

import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbDate
import org.scalacheck.rng.Seed

trait DemoTestProvider {
  implicit val arbLocalDate = Arbitrary
    .apply(arbDate.arbitrary.map(_.toInstant.atZone(ZoneId.systemDefault()).toLocalDate()))
  def parameter: Gen.Parameters = Gen.Parameters.default
  def sampleSize: Int = 50
  def retries: Int = 150
  def seed: Seed = Seed.random()
}
