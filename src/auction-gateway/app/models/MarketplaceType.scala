package models
import enumeratum.values.{StringEnum, StringEnumEntry}

sealed class MarketplaceType(override val value: String) extends StringEnumEntry

object MarketplaceType extends StringEnum[MarketplaceType] {

  object Open extends MarketplaceType("open")
  object Pb   extends MarketplaceType("pb")
  object OpenFirstPrice extends MarketplaceType("ofp")

  override def values = findValues
}
