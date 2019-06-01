package services.auction.pb.adapters.pubnative.model

import com.appodealx.exchange.common.models.circe.CirceEnumInstances
import enumeratum.values.{StringEnum, StringEnumEntry}


sealed abstract class BeaconType(override val value: String) extends StringEnumEntry

object BeaconType extends StringEnum[BeaconType] with CirceEnumInstances {

  object Impression extends BeaconType("impression")

  object Click extends BeaconType("click")

  val values = findValues

}