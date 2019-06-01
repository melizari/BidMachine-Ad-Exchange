package services.auction.pb.adapters.smaato.model

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class SmaatoParams(pub: Int,
                        adspace: Int,
                        dimension: Option[Dimensions.Value],
                        videotype: Option[VideoTypes.Value])

object Dimensions extends Enumeration {
  val xxlarge, medrect, sky, leader, `full_320x480`, `full_640x960`, `full_640x1136`, `full_768x1024`, `full_800x1280` =
    Value
}

object VideoTypes extends Enumeration {
  val `instream-pre`, `instream-mid`, `instream-post`, outstream, interstitial, rewarded = Value
}

object SmaatoParams {
  implicit val dimensionsDecoder            = Decoder.enumDecoder(Dimensions)
  implicit val videoTypesDecoder            = Decoder.enumDecoder(VideoTypes)
  implicit val smaatoExtensionObjectDecoder = deriveDecoder[SmaatoParams]
}
