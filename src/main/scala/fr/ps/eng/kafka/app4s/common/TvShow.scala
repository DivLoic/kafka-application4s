package fr.ps.eng.kafka.app4s.common

import kantan.csv._
import kantan.csv.enumeratum._

/**
 * Created by loicmdivad.
 */
case class TvShow(platform: Platform, name: String, releaseYear: Int, imdb: Option[Double])

object TvShow {

  implicit val tvShowRowDecoder: RowDecoder[TvShow] = RowDecoder.ordered {
    (platform: Platform, name: String, releaseYear: Int, imdb: Option[Double]) =>
      TvShow(platform, name, releaseYear, imdb)
  }
}