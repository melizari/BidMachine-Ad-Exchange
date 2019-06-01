package com.appodealx.openrtb

import enumeratum.values.{IntEnum, IntEnumEntry}


sealed abstract class ProductionQuality(override val value: Int) extends IntEnumEntry with Serializable


object ProductionQuality extends IntEnum[ProductionQuality] {

  object Unknown extends ProductionQuality(0)

  object ProfessionallyProduced extends ProductionQuality(1)

  object Prosumer extends ProductionQuality(2)

  object UserGenerated extends ProductionQuality(3)

  val values = findValues

}
