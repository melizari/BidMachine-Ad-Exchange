package services.auction.pb.adapters.rubicon.model.extensions

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

case class VideoRubiconAdditions(`size_id`: Option[Int])

case class VideoRubiconExtension(skipdelay: Option[Int] = Some(0),
                                 skip: Option[Int] = Some(0),
                                 placement: Option[Int] = None,
                                 rp: VideoRubiconAdditions)

object VideoRubiconExtension {
  implicit val videoRubiconAdditionsEnc: Encoder[VideoRubiconAdditions] = deriveEncoder[VideoRubiconAdditions]
  implicit val videoRubiconExtensionEnc: Encoder[VideoRubiconExtension] = deriveEncoder[VideoRubiconExtension]
}


