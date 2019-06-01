package com.appodealx.exchange.settings.persistance.common
import com.appodealx.openrtb._
import com.appodealx.exchange.common.db.PostgresProfile.api._

trait RtbNativeColumns { self: Table[_] =>

  import com.appodealx.exchange.settings.persistance.common.LiftedRtbInstances._

  def rtbNativeVer = column[Option[String]]("rtb_native_ver")
  def rtbNativeApi = column[Option[List[ApiFramework]]]("rtb_native_api")
  def rtbNativeBattr = column[Option[List[CreativeAttribute]]]("rtb_native_battr")
  def rtbNativeRequest = column[Option[String]]("rtb_native_request")
  def rtbNativeExt = column[Option[Json]]("rtb_native_ext")

  def rtbNative = LiftedRtbNative(
    rtbNativeVer,
    rtbNativeApi,
    rtbNativeBattr,
    rtbNativeRequest,
    rtbNativeExt)

}
