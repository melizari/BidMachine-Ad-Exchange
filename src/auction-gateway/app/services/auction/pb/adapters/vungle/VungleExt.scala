package services.auction.pb.adapters.vungle

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, ObjectEncoder}

case class BidToken(`bid_token`: String)

object BidToken {
  implicit val bidTokenDecoder: Decoder[BidToken] = deriveDecoder[BidToken]
  implicit val bidTokenEncoder: ObjectEncoder[BidToken] = deriveEncoder[BidToken]
}

case class VungleExt(vungle: BidToken)

object VungleExt {
  implicit val vungleExtDecoder: Decoder[VungleExt] = deriveDecoder[VungleExt]
  implicit val vungleExtEncoder: ObjectEncoder[VungleExt] = deriveEncoder[VungleExt]
}

