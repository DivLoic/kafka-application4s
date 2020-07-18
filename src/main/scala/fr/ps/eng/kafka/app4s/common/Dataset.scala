package fr.ps.eng.kafka.app4s.common

import kantan.csv._
import kantan.csv.ops._
/**
 * Created by loicmdivad.
 */
object Dataset {

  val AllTvShows: Map[Key, TvShow] = load("/dataset.csv")

  val `200TvShows`: Map[Key, TvShow] = load("/extract.csv")

  private def load(filePath: String) = getClass.getResource(filePath).toURI
    .asCsvReader[TvShow](rfc.withoutHeader)
    .filter(_.isRight)
    .collect(_.toTry.get)
    .map(show => (Key(sha1(show.name)), show))
    .toVector.toMap
}
