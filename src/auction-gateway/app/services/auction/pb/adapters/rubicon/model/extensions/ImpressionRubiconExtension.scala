package services.auction.pb.adapters.rubicon.model.extensions

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class ImpressionRubiconAdditions(`zone_id`: Int, enc: Option[String] = None)

case class ImpressionRubiconExtension(rp: ImpressionRubiconAdditions,
                                      viewabilityvendors: Option[List[String]] = None)

object ImpressionRubiconExtension {
  implicit val impressionRubiconAdditionsDec: Decoder[ImpressionRubiconAdditions] = deriveDecoder[ImpressionRubiconAdditions]
  implicit val impressionRubiconExtensionDec: Decoder[ImpressionRubiconExtension] = deriveDecoder[ImpressionRubiconExtension]

  implicit val impressionRubiconAdditionsEnc: Encoder[ImpressionRubiconAdditions] = deriveEncoder[ImpressionRubiconAdditions]
  implicit val impressionRubiconExtensionEnc: Encoder[ImpressionRubiconExtension] = deriveEncoder[ImpressionRubiconExtension]
}
