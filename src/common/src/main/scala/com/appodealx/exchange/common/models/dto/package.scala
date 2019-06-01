package com.appodealx.exchange.common.models

package object dto {

  type BannerAndVideo = (Banner, Video)

  type Rewarded     = BannerAndVideo
  type Interstitial = BannerAndVideo
}
