package services.auction.pb.adapters.criteo.s2s.model.rs
import io.circe.derivation.deriveDecoder
import io.circe.derivation.deriveEncoder

private[s2s] case class Creative(image_url: String,
                    click_url: String,
                    title: String,
                    description: String,
                    price: String,
                    call_to_action: String,
                    imp_trackers: List[String],
                    view_notice_tracker: String)
private[s2s] object Creative {
  implicit val decoder = deriveDecoder[Creative]
  implicit val encoder = deriveEncoder[Creative]
}
