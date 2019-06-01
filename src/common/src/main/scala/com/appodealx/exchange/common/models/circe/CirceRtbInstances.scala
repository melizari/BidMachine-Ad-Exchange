package com.appodealx.exchange.common.models.circe

import com.appodealx.openrtb._
import com.appodealx.openrtb.native._
import io.circe._
import io.circe.derivation.{deriveDecoder, deriveEncoder}


trait CirceRtbInstances extends CirceEnumInstances {

  private implicit val rtbBooleanEncoder = Encoder.encodeInt.contramap[Boolean](if (_) 1 else 0)
  private implicit val rtbBooleanDecoder = Decoder.decodeInt.map(_ > 0)

  implicit val rtbProducerDecoder = deriveDecoder[Producer]
  implicit val rtbProducerEncoder = deriveEncoder[Producer]

  implicit val rtbPublisherDecoder = deriveDecoder[Publisher]
  implicit val rtbPublisherEncoder = deriveEncoder[Publisher]

  implicit val rtbSegmentDecoder = deriveDecoder[Segment]
  implicit val rtbSegmentEncoder = deriveEncoder[Segment]

  implicit val rtbDataDecoder = deriveDecoder[Data]
  implicit val rtbDataEncoder = deriveEncoder[Data]

  implicit val rtbContentDecoder = deriveDecoder[Content]
  implicit val rtbContentEncoder = deriveEncoder[Content]

  implicit val rtbAppDecoder = deriveDecoder[App]
  implicit val rtbAppEncoder = deriveEncoder[App]

  implicit val rtbSiteDecoder = deriveDecoder[Site]
  implicit val rtbSiteEncoder = deriveEncoder[Site]

  implicit val rtbGeoDecoder = deriveDecoder[Geo]
  implicit val rtbGeoEncoder = deriveEncoder[Geo]

  implicit val rtbDeviceDecoder = deriveDecoder[Device]
  implicit val rtbDeviceEncoder = deriveEncoder[Device]

  implicit val rtbFormatDecoder = deriveDecoder[Format]
  implicit val rtbFormatEncoder = deriveEncoder[Format]

  implicit val rtbBannerDecoder = deriveDecoder[Banner]
  implicit val rtbBannerEncoder = deriveEncoder[Banner]

  implicit val rtbNativeDecoder = deriveDecoder[Native]
  implicit val rtbNativeEncoder = deriveEncoder[Native]

  implicit val rtbVideoDecoder = deriveDecoder[Video]
  implicit val rtbVideoEncoder = deriveEncoder[Video]

  implicit val rtbAudioDecoder = deriveDecoder[Audio]
  implicit val rtbAudioEncoder = deriveEncoder[Audio]

  implicit val rtbDealDecoder = deriveDecoder[Deal]
  implicit val rtbDealEncoder = deriveEncoder[Deal]

  implicit val rtbPmpDecoder = deriveDecoder[Pmp]
  implicit val rtbPmpEncoder = deriveEncoder[Pmp]

  implicit val rtbMetricDecoder = deriveDecoder[Metric]
  implicit val rtbMetricEncoder = deriveEncoder[Metric]

  implicit val rtbImpDecoder = deriveDecoder[Imp]
  implicit val rtbImpEncoder = deriveEncoder[Imp]

  implicit val rtbUserDecoder = deriveDecoder[User]
  implicit val rtbUserEncoder = deriveEncoder[User]

  implicit val rtbRegsDecoder = deriveDecoder[Regs]
  implicit val rtbRegsEncoder = deriveEncoder[Regs]

  implicit val rtbSourceDecoder = deriveDecoder[Source]
  implicit val rtbSourceEncoder = deriveEncoder[Source]

  implicit val rtbBidRequestDecoder = deriveDecoder[BidRequest]
  implicit val rtbBidRequestEncoder = deriveEncoder[BidRequest]

  implicit val rtbBidDecoder = deriveDecoder[Bid]
  implicit val rtbBidEncoder = deriveEncoder[Bid]

  implicit val rtbSeatBidDecoder = deriveDecoder[SeatBid]
  implicit val rtbSeatBidEncoder = deriveEncoder[SeatBid]

  implicit val rtbBidResponseDecoder = deriveDecoder[BidResponse]
  implicit val rtbBidResponseEncoder = deriveEncoder[BidResponse]

  implicit val rtbNativeRequestTitleDecoder = deriveDecoder[request.Title]
  implicit val rtbNativeRequestTitleEncoder = deriveEncoder[request.Title]

  implicit val rtbNativeRequestImageDecoder = deriveDecoder[request.Image]
  implicit val rbtNativeRequestImageEncoder = deriveEncoder[request.Image]

  implicit val rtbNativeRequestVideoDecoder = deriveDecoder[request.Video]
  implicit val rtbNativeRequestVideoEncoder = deriveEncoder[request.Video]

  implicit val rtbNativeRequestDataDecoder = deriveDecoder[request.Data]
  implicit val rtbNativeRequestDataEncoder = deriveEncoder[request.Data]

  implicit val rtbNativeRequestAssetDecoder = deriveDecoder[request.Asset]
  implicit val rtbNativeRequestAssetEncoder = deriveEncoder[request.Asset]

  implicit val rtbNativeRequestDecoder = deriveDecoder[request.Native]
  implicit val rtbNativeRequestEncoder = deriveEncoder[request.Native]

  implicit val rtbNativeResponseTitleDecoder = deriveDecoder[response.Title]
  implicit val rtbNativeResponseTitleEncoder = deriveEncoder[response.Title]

  implicit val rtbNativeResponseImageDecoder = deriveDecoder[response.Image]
  implicit val rtbNativeResponseImageEncoder = deriveEncoder[response.Image]

  implicit val rtbNativeResponseVideoDecoder = deriveDecoder[response.Video]
  implicit val rtbNativeResponseVideoEncoder = deriveEncoder[response.Video]

  implicit val rtbNativeResponseLinkDecoder = deriveDecoder[response.Link]
  implicit val rtbNativeResponseLinkEncoder = deriveEncoder[response.Link]

  implicit val rtbNativeResponseDataDecoder = deriveDecoder[response.Data]
  implicit val rtbNativeResponseDataEncoder = deriveEncoder[response.Data]

  implicit val rtbNativeResponseAssetDecoder = deriveDecoder[response.Asset]
  implicit val rtbNativeResponseAssetEncoder = deriveEncoder[response.Asset]

  private val nativeDecoder = deriveDecoder[response.Native]
  private val nestedNativeDecoder = nativeDecoder.prepare(_.downField("native"))

  implicit val rtbNativeResponseDecoder = nestedNativeDecoder or nativeDecoder
  implicit val rtbNativeResponseEncoder = deriveEncoder[response.Native]
}
