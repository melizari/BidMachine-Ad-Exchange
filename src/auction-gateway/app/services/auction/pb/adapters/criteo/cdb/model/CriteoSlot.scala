package services.auction.pb.adapters.criteo.cdb.model

import io.circe.derivation._


case class CriteoSlot(impid: String,
                      cpm: Double,
                      displayurl: Option[String],
                      zoneid: Int,
                      width: Option[Int],
                      height: Option[Int],
                      native: Option[Native])

object CriteoSlot {
  implicit val decoder = deriveDecoder[CriteoSlot]
  implicit val encoder = deriveEncoder[CriteoSlot]
}