package silhouette

import enumeratum.values.{StringEnum, StringEnumEntry}

sealed abstract class ResourceType(val value: String) extends StringEnumEntry

object ResourceType extends StringEnum[ResourceType] {

  case object Seller extends ResourceType("seller")
  case object Agency extends ResourceType("agency")

  def values = findValues
}
