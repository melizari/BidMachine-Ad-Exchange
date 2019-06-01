package services.auction.pb.adapters.criteo.s2s.model.rq

import io.circe.derivation.deriveEncoder

private[s2s] case class Publisher(id: String, domain: Option[String])

private[s2s] object Publisher {
  implicit val encoder = deriveEncoder[Publisher]
}
