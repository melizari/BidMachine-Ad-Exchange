package com.appodealx.exchange.common.models

object CallbackMacros {

  //${APPODEALX_SEGMENT_ID}
  val SegmentId = "APPODEALX_SEGMENT_ID"

  //${APPODEALX_PLACEMENT_ID}
  val PlacementId = "APPODEALX_PLACEMENT_ID"

  val SegmentIdMacros = "%%SEGMENT%%"
  val PlacementIdMacros = "%%PLACEMENT%%"

  val EventCodeMacros = "BM_EVENT_CODE"
  val ActionCodeMacros = "BM_ACTION_CODE"
  val ErrorReasonMacros = "BM_ERROR_REASON"

  val LegacyErrorCodePercent = "%%ERRORCODE%%"
  val LegacyErrorCode = "[ERRORCODE]"
}
