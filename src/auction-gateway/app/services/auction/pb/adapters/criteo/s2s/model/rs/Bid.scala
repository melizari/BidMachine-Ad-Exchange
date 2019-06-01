package services.auction.pb.adapters.criteo.s2s.model.rs
import io.circe.derivation.deriveDecoder
import io.circe.derivation.deriveEncoder

private[s2s] case class Bid(impid: String,
                            traceid: String,
                            lurl: Option[String],
                            bidprice: Double,
                            currency: String,
                            dealid: Option[String],
                            adomain: String,
                            crid: String,
                            fillimages: Option[Int],
                            adm: Option[String],
                            w: Option[Int],
                            h: Option[Int],
                            creative: Option[Creative],
                            payload: String,
                            privacy: Option[Privacy])
private[s2s] object Bid {
  implicit val decoder = deriveDecoder[Bid]
  implicit val encoder = deriveEncoder[Bid]
}
