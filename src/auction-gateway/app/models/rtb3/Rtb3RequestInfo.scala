package models.rtb3

case class Rtb3RequestInfo(dealId: DealId, reqId: ReqId, itemId: ItemId)

object Rtb3RequestInfo {
  def of(rtb3Req: Rtb3Request): Rtb3RequestInfo = {
    val dealId = rtb3Req.item.deal.headOption.map(d => DealId(d.id)).getOrElse(DealId(""))
    val reqId  = ReqId(rtb3Req.reqId)
    val itemId = ItemId(rtb3Req.item.id)

    Rtb3RequestInfo(dealId, reqId, itemId)
  }
}

final case class DealId(value: String) extends AnyVal with Product
final case class ReqId(value: String) extends AnyVal with Product
final case class ItemId(value: String) extends AnyVal with Product