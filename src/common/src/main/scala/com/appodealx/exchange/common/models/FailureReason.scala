package com.appodealx.exchange.common.models

import enumeratum.{Enum, EnumEntry}


sealed abstract class FailureReason(override val entryName: String) extends EnumEntry

object FailureReason extends Enum[FailureReason] {

  object RequestValidatingFailure extends FailureReason("request_validation_error")

  object RequestDecodingFailure extends FailureReason("request_decoding_error")

  object RequestParamFailure extends FailureReason("request_param_error")

  object RtbAdmDecodingFailure extends FailureReason("rtb_adm_decoding_error")

  object RtbAdmMissingFailure extends FailureReason("rtb_adm_missing_error")

  object RequestMissingParametersFailure extends FailureReason("request_parameters_missing_error")

  object InternalFailure extends FailureReason("internal_error")

  object SellerStatus extends FailureReason("seller_status")

  object AdSpaceStatus extends FailureReason("ad_space_status")

  object AdmPrepareFailure extends FailureReason("adm_prepare_error")

  object AdNetworkClientNotFoundFailure extends FailureReason("ad_network_client_not_found_in_registry")

  object AppValidatingError extends FailureReason("app_validating_error")

  object AppDecodingFailure extends FailureReason("app_decoding_failure")

  val values = findValues

}