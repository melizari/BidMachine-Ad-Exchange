package services.auction.pb.adapters.criteo.s2s.model.rq

import io.circe.derivation.deriveEncoder

private[s2s] case class Site(id: String, page: String, domain: Option[String])

private[s2s] object Site {
  implicit val encoder = deriveEncoder[Site]
}
