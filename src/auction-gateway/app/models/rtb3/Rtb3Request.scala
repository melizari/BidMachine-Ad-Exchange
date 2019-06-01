package models.rtb3
import io.bidmachine.protobuf.RequestExtension
import io.bidmachine.protobuf.adcom.{Context, Placement}
import io.bidmachine.protobuf.openrtb.Request.Item

case class Rtb3Request(plc: Placement,
                       ctx: Context,
                       item: Item,
                       reqExt: RequestExtension,
                       reqId: String,
                       test: Boolean)
