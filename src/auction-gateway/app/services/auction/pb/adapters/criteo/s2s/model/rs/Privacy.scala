package services.auction.pb.adapters.criteo.s2s.model.rs
import io.circe.derivation.deriveDecoder
import io.circe.derivation.deriveEncoder

private[s2s] case class Privacy(image_url: String, click_url: String)

private[s2s] object Privacy {
  implicit val decoder = deriveDecoder[Privacy]
  implicit val encoder = deriveEncoder[Privacy]
}
