package com.appodealx.openrtb.native

import enumeratum.values.{IntEnum, IntEnumEntry}


sealed abstract class ContextType(override val value: Int) extends IntEnumEntry with Serializable


object ContextType extends IntEnum[ContextType] {

  object ContentCentric extends ContextType(1)

  object SocialCentric extends ContextType(2)

  object ProductContext extends ContextType(3)

  val values = findValues

}