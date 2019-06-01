package com.appodealx.exchange.common.models.dto

import com.appodealx.exchange.common.models.NativeRequest
import com.appodealx.exchange.common.models.jsoniter.JsoniterRtbInstances
import com.appodealx.openrtb
import com.appodealx.openrtb.{ApiFramework, CreativeAttribute, Json}

object Native extends JsoniterRtbInstances

case class Native(ver: Option[String] = None,
                  api: Option[List[ApiFramework]] = None,
                  battr: Option[List[CreativeAttribute]] = None,
                  request: Option[String] = None,
                  ext: Option[Json] = None) { self =>

  def withDefaults = copy(
    ver = self.ver.orElse(Some("1.1"))
  )

  import Native.rtbNativeRequestCodec
  import com.github.plokhotnyuk.jsoniter_scala.core._

  def toRtb = openrtb.Native(
    request = request.getOrElse(new String(writeToArray(NativeRequest.Default))),
    ver = ver,
    api = api.map(_.toList),
    battr = battr.map(_.toList),
    ext = ext
  )
}
