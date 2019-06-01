package com.appodealx.exchange.common.models.auction

import enumeratum.values.{StringEnum, StringEnumEntry}

import scala.collection.immutable


sealed abstract class Protocol(override val value: String) extends StringEnumEntry with Serializable

object Protocol extends StringEnum[Protocol] {

  object AdColony extends Protocol("adcolony")
  object AppLovin extends Protocol("applovin")
  object Criteo extends Protocol("criteo")
  object CriteoS2S extends Protocol("criteo_s2s")
  object Facebook extends Protocol("facebook")
  @deprecated object Mailru extends Protocol("mailru")
  @deprecated object MyTargetOld extends Protocol("mytarget")
  object MyTarget extends Protocol("my_target")
  object OpenRTB extends Protocol("openrtb")
  object Pubnative extends Protocol("pubnative")
  object Rubicon extends Protocol("rubicon")
  object Smaato extends Protocol("smaato")
  object Tapjoy extends Protocol("tapjoy")
  object Vungle extends Protocol("vungle")
  object OpenX extends Protocol("openx")
  object Pubmatic extends Protocol("pubmatic")
  object HangMyAds extends Protocol("hangmyads")


  override def values: immutable.IndexedSeq[Protocol] = findValues
}