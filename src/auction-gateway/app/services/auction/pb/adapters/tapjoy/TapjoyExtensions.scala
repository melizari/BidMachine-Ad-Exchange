package services.auction.pb.adapters.tapjoy

import io.circe._
import io.circe.generic.semiauto._
import io.circe.parser._

import cats.syntax.either._
import cats.syntax.applicativeError._

case class TapjoyPlacement(`sdk_key`: String, `placement_name`: String, token: Option[String])

object TapjoyPlacement {
  implicit val placementDecoder = deriveDecoder[TapjoyPlacement]
  implicit val placementEncoder = deriveEncoder[TapjoyPlacement]
}

case class TapjoyVideoExtension(rewarded: Int, skippable: Option[Int])

object TapjoyVideoExtension {
  implicit val videoExtensionEncoder = deriveEncoder[TapjoyVideoExtension]
  implicit val videoExtensionDecoder = deriveDecoder[TapjoyVideoExtension]
}

case class TapjoyBidExtension(`tapjoy_metadata`: Map[String, String] = Map())

object TapjoyBidExtension {

  private val metadataDecoder = Decoder.instance { cur =>
    for {
      s <- cur.as[String]
      j <- parse(s).leftMap(e => DecodingFailure(e.message, Nil))
      m <- j.as[Map[String, String]]
    } yield m
  }

  implicit val bidExtensionDecoder = Decoder
    .forProduct1("tj_data")(TapjoyBidExtension.apply)(metadataDecoder)
    .handleError(_ => TapjoyBidExtension())

  implicit val bidExtensionEncoder = deriveEncoder[TapjoyBidExtension]

}
