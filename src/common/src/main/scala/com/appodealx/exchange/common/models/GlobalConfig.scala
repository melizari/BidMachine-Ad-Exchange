package com.appodealx.exchange.common.models

case class GlobalConfig(tMax: Option[Int] = None,
                        forceNoFill: Option[Boolean] = None)

                        //                        debugTopic: Option[String] = None,
                        //                        debugIsInterstitial: Option[Boolean] = None,
                        //                        debugIsBanner: Option[Boolean] = None,
                        //                        debugIsVideo: Option[Boolean] = None,
                        //                        debugIsNative: Option[Boolean] = None,
                        //                        debugAppId: Option[Int] = None,
                        //                        debugBidderId: Option[Int] = None,
                        //                        debugOnlyBid: Option[Boolean] = None,
                        //                        debugWithADM: Option[Boolean] = None,


object GlobalConfig {

  val version = 7

}
