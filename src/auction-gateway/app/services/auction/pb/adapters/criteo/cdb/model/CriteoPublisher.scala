package services.auction.pb.adapters.criteo.cdb.model

import io.circe.derivation.deriveEncoder


case class CriteoPublisher(bundleid: String, publisherid: Option[String])

object CriteoPublisher {
  implicit val encoder = deriveEncoder[CriteoPublisher]
}