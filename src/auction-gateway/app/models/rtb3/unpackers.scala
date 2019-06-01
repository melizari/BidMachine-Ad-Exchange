package models.rtb3

import io.bidmachine.protobuf.{PlacementExtension, RequestExtension}
import io.bidmachine.protobuf.adcom.{Context, Placement}
import io.bidmachine.protobuf.adcom.Placement.{DisplayPlacement, VideoPlacement}
import io.bidmachine.protobuf.openrtb.Openrtb

object unpackers {
  def placementOf(op: Openrtb) = {
    val request      = op.payload.request.get
    val item         = request.item.head
    val placementAny = item.spec.get

    placementAny.unpack(Placement)
  }

  def requestExtOf(op: Openrtb) = {
    val request       = op.payload.request.get
    val requestExtAny = request.ext.find(_.is[RequestExtension]).get

    requestExtAny.unpack[RequestExtension]
  }

  def contextOf(op: Openrtb) = {
    val request    = op.payload.request.get
    val contextAny = request.context.get

    contextAny.unpack(Context)
  }

  def placementExtOf(d: Option[DisplayPlacement], v: Option[VideoPlacement]) = {
    val ext = d.fold(v.get.ext)(_.ext)

    ext.head.unpack(PlacementExtension)
  }
}
