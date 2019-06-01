package services.auction.pb.adapters.criteo.s2s.model.rq

import io.circe.derivation.deriveEncoder

private[s2s] case class Deal(id: String, floorprice: Double)

private[s2s] object Deal {
  implicit val encoder = deriveEncoder[Deal]
}
