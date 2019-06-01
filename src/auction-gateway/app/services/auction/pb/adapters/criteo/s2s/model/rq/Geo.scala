package services.auction.pb.adapters.criteo.s2s.model.rq

import io.circe.derivation.deriveEncoder

private[s2s] case class Geo(latitude: Option[Double], longitude: Option[Double])

private[s2s] object Geo {
  implicit val encoder = deriveEncoder[Geo]
}
