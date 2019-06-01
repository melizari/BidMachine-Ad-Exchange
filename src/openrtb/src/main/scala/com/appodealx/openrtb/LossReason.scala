package com.appodealx.openrtb

import enumeratum.values.{IntEnum, IntEnumEntry}


sealed abstract class LossReason(override val value: Int) extends IntEnumEntry with Serializable


object LossReason extends IntEnum[LossReason] {

  object BidWon extends LossReason(0)

  object InternalError extends LossReason(1)

  object ImpressionOpportunityExpired extends LossReason(2)

  object InvalidBidResponse extends LossReason(3)

  object InvalidDealId extends LossReason(4)

  object InvalidAuctionId extends LossReason(5)

  object InvalidAdDomain extends LossReason(6)

  object MissingMarkup extends LossReason(7)

  object MissingCreativeId extends LossReason(8)

  object MissingBidPrice extends LossReason(9)

  object MissingMinCreativeApprovalData extends LossReason(10)

  object BidBelowAuctionFloor extends LossReason(100)

  object BidBelowDealFloor extends LossReason(101)

  object LostToHigherBid extends LossReason(102)

  object LostToBidForPMPDeal extends LossReason(103)

  object BuyerSeatBlocked extends LossReason(104)

  object CreativeFilteredGeneral extends LossReason(200)

  object CreativeFilteredPendingApproval extends LossReason(201)

  object CreativeFilteredDisapproved extends LossReason(202)

  object CreativeFilteredSizeNotAllowed extends LossReason(203)

  object CreativeFilteredIncorrectCreativeFormat extends LossReason(204)

  object CreativeFilteredAdvertiserExclusions extends LossReason(205)

  object CreativeFilteredAppBundleExclusions extends LossReason(206)

  object CreativeFilteredNotSecure extends LossReason(207)

  object CreativeFilteredLanguageExclusions extends LossReason(208)

  object CreativeFilteredCategoryExclusions extends LossReason(209)

  object CreativeFilteredCreativeAttributeExclusions extends LossReason(210)

  object CreativeFilteredAdTypeExclusions extends LossReason(211)

  object CreativeFilteredAnimationTooLong extends LossReason(212)

  object CreativeFilteredNotAllowedInPMPDeal extends LossReason(213)

  val values = findValues

}