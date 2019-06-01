package services.auction.pb.adapters.hangmyads.model.rs

import io.circe.derivation.deriveDecoder

private[hangmyads] case class Ads(count: Int, ad: Option[List[Ad]])

private[hangmyads] object Ads {
  implicit val decoder = deriveDecoder[Ads]
}
