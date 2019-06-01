package services.auction.pb.adapters.criteo.s2s.model.rq

import io.circe.derivation.deriveEncoder

private[s2s] case class Size(w: Int, h: Int)

private[s2s] object Size {
  implicit val encoder = deriveEncoder[Size]
}
