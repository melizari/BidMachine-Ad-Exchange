package com.appodealx.exchange.common.models.jsoniter

import com.appodealx.exchange.common.utils.jsoniter.CirceJsonValueCodecInstances
import com.appodealx.openrtb.native.request.Native
import com.appodealx.openrtb.{BidRequest, BidResponse}
import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._

trait JsoniterRtbInstances
  extends JsoniterEnumInstances
    with CirceJsonValueCodecInstances {

  private implicit val rtbBooleanCodec = new JsonValueCodec[Boolean] {
    override def decodeValue(in: JsonReader, default: Boolean) = in.readInt() > 0

    override def encodeValue(x: Boolean, out: JsonWriter): Unit = if (x) out.writeVal(1) else out.writeVal(0)

    override def nullValue = false
  }

  implicit val rtbBidRequestCodec = JsonCodecMaker.make[BidRequest](CodecMakerConfig())
  implicit val rtbBidResponseCodec = JsonCodecMaker.make[BidResponse](CodecMakerConfig())
  implicit val rtbNativeRequestCodec = JsonCodecMaker.make[Native](CodecMakerConfig())

}
