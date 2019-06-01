package services.auction.pb.adapters.rubicon.model.extensions

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

case class BannerRubiconAdditions(`size_id`: Option[Int] = None,
                                  `alt_size_ids`: Option[List[Int]] = None,
                                  mime: Option[String] = None,
                                  usenurl: Option[Int] = Some(1),
                                  useimptrackers: Option[Int] = Some(0))

case class BannerRubiconExtension(rp: BannerRubiconAdditions)

object BannerRubiconExtension {
  implicit val bannerRubiconAdditionsEnc: Encoder[BannerRubiconAdditions] = deriveEncoder[BannerRubiconAdditions]
  implicit val bannerRubiconExtensionEnc: Encoder[BannerRubiconExtension] = deriveEncoder[BannerRubiconExtension]
}
