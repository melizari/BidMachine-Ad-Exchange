package com.appodealx.openrtb.native.request

import com.appodealx.openrtb.native._

case class Native(assets: List[Asset],
                  ver: Option[String] = None,
                  context: Option[ContextType] = None,
                  contentsubtype: Option[ContextSubtype] = None,
                  plcmttype: Option[PlacementType] = None,
                  plcmtcnt: Option[Int] = Some(1),
                  seq: Option[Int] = Some(0))
