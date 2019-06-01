package com.appodealx.exchange.common.models

import com.appodealx.openrtb
import com.appodealx.openrtb._
import com.appodealx.openrtb.native._


package object rtb {

  implicit class AdPositionOps(adPosition: openrtb.AdPosition) extends Pretty {
    override def prettyValue: String = {
      adPosition match {
        case AdPosition.Unknown => "unknown"
        case AdPosition.AboveTheFold => "above_the_fold"
        case AdPosition.MaybeNotVisible => "maybe_not_visible"
        case AdPosition.BelowTheFold => "below_the_fold"
        case AdPosition.Header => "header"
        case AdPosition.Footer => "footer"
        case AdPosition.Sidebar => "sidebar"
        case AdPosition.FullScreen => "full_screen"
      }
    }
  }

  implicit class ApiFrameworkOps(apiFramework: openrtb.ApiFramework) extends Pretty {
    override def prettyValue: String = apiFramework match {
      case ApiFramework.VPAID_1 => "vpaid_1"
      case ApiFramework.VPAID_2 => "vpaid_2"
      case ApiFramework.MRAID_1 => "mraid_1"
      case ApiFramework.ORMMA => "ormma"
      case ApiFramework.MRAID_2 => "mraid_2"
      case ApiFramework.MRAID_3 => "mraid_3"
    }
  }

  implicit class AuctionTypeOps(at: openrtb.AuctionType) extends Pretty {
    override def prettyValue: String = at match {
      case AuctionType.FirstPrice => "first_price"
      case AuctionType.SecondPrice => "second_price"
      case AuctionType.DealPrice => "deal_price"
    }
  }

  implicit class BannerAdTypeOps(bat: openrtb.BannerAdType) extends Pretty {
    override def prettyValue: String = bat match {
      case BannerAdType.XHTMLTextAd => "xhtml_text_ad"
      case BannerAdType.XHTMLBannerAd => "xhtml_banner_ad"
      case BannerAdType.JavaScriptAd => "javascript_ad"
      case BannerAdType.Iframe => "iframe"
    }
  }

  implicit class CompanionTypeOps(ct: openrtb.CompanionType) extends Pretty {
    override def prettyValue: String = ct match {
      case CompanionType.StaticResource => "static_resource"
      case CompanionType.HTMLResource => "html_resource"
      case CompanionType.IframeResource => "iframe_resource"
    }
  }

  implicit class ConnectionTypeOps(ct: openrtb.ConnectionType) extends Pretty {
    override def prettyValue: String = ct match {
      case ConnectionType.Unknown => "unknown"
      case ConnectionType.Ethernet => "ethernet"
      case ConnectionType.Wifi => "wifi"
      case ConnectionType.CellularUnknownGen => "cellular_unknown_get"
      case ConnectionType.Cellular2G => "cellular_2g"
      case ConnectionType.Cellular3G => "cellular_3g"
      case ConnectionType.Cellular4G => "cellular_4g"
    }
  }

  implicit class ContentContextOps(cc: openrtb.ContentContext) extends Pretty {
    override def prettyValue: String = cc match {
      case ContentContext.Video => "video"
      case ContentContext.Game => "game"
      case ContentContext.Music => "music"
      case ContentContext.Application => "application"
      case ContentContext.Text => "text"
      case ContentContext.Other => "other"
      case ContentContext.Unknown => "unknown"
    }
  }

  implicit class ContentDeliveryMethodOps(cdm: openrtb.ContentDeliveryMethod) extends Pretty {
    override def prettyValue: String = cdm match {
      case ContentDeliveryMethod.Streaming => "streaming"
      case ContentDeliveryMethod.Progressive => "progressive"
    }
  }

  implicit class CreativeAttributeOps(ca: openrtb.CreativeAttribute) extends Pretty {
    override def prettyValue: String = ca match {
      case CreativeAttribute.AudioAdAutoPlay => "1_audio_ad_auto_play"
      case CreativeAttribute.AudioAdUserInit => "2_audio_ad_user_initiated"
      case CreativeAttribute.ExpandableAutomatic => "3_expandable_automatic"
      case CreativeAttribute.ExpandableUserClick => "4_expandable_user_click"
      case CreativeAttribute.ExpandableUserRollover => "5_expandable_user_rollover"
      case CreativeAttribute.InBannerVideoAdAutoPlay => "6_in_banner_video_ad_auto_play"
      case CreativeAttribute.InBannerVideoAdUserInit => "7_in_banner_video_ad_user_init"
      case CreativeAttribute.Pop => "8_pop"
      case CreativeAttribute.Provocative => "9_provocative"
      case CreativeAttribute.EpilepsyWarning => "10_epilepsy_warning"
      case CreativeAttribute.Surveys => "11_surveys"
      case CreativeAttribute.TextOnly => "12_text_only"
      case CreativeAttribute.UserInteractive => "13_user_interactive"
      case CreativeAttribute.AlertStyle => "14_alert_style"
      case CreativeAttribute.HasAudionButton => "15_has_audion_button"
      case CreativeAttribute.AdCanBeSkipped => "16_ad_can_be_skipped"
      case CreativeAttribute.AdobeFlash => "17_adobe_flash"
    }
  }

  implicit class DeviceTypeOps(dt: openrtb.DeviceType) extends Pretty {
    override def prettyValue: String = dt match {
      case DeviceType.Mobile => "mobile"
      case DeviceType.PC => "personal_computer"
      case DeviceType.TV => "connected_tv"
      case DeviceType.Phone => "phone"
      case DeviceType.Tablet => "tablet"
      case DeviceType.ConnectedDevice => "connected_device"
      case DeviceType.SetTopBox => "set_top_box"
    }
  }

  implicit class ExpandableDirectionOps(ed: openrtb.ExpandableDirection) extends Pretty {
    override def prettyValue: String = ed match {
      case ExpandableDirection.Left => "left"
      case ExpandableDirection.Right => "right"
      case ExpandableDirection.Up => "up"
      case ExpandableDirection.Down => "down"
      case ExpandableDirection.FullScreen => "full_screen"
    }
  }

  implicit class FeedTypeOps(ft: openrtb.FeedType) extends Pretty {
    override def prettyValue: String = ft match {
      case FeedType.MusicService => "music_service"
      case FeedType.RadioBroadcast => "radio_broadcast"
      case FeedType.Podcast => "podcast"
    }
  }

  implicit class GenderOps(g: openrtb.Gender) extends Pretty {
    override def prettyValue: String = g match {
      case Gender.Male => "male"
      case Gender.Female => "female"
      case Gender.Other => "other"
    }
  }

  implicit class IpLocationServiceOps(ils: openrtb.IpLocationService) extends Pretty {
    override def prettyValue: String = ils match {
      case IpLocationService.IpToLocation => "ip_to_location"
      case IpLocationService.Neustar => "neustar"
      case IpLocationService.MaxMind => "maxmind"
      case IpLocationService.NetAquity => "netaquity"
      case IpLocationService.Unknown => "unknown"
      case IpLocationService.Sypex => "sypex"
    }
  }

  implicit class LocationTypeOps(lt: openrtb.LocationType) extends Pretty {
    override def prettyValue: String = lt match {
      case LocationType.GPS => "gps"
      case LocationType.IP => "ip"
      case LocationType.UserProvided => "user_provided"
    }
  }

  implicit class LossReasonOps(lr: openrtb.LossReason) extends Pretty {
    override def prettyValue: String = lr match {
      case LossReason.BidWon => "bid_won"
      case LossReason.InternalError => "internal_error"
      case LossReason.ImpressionOpportunityExpired => "impression_opportunity_expired"
      case LossReason.InvalidBidResponse => "invalid_bid_response"
      case LossReason.InvalidDealId => "invalid_deal_id"
      case LossReason.InvalidAuctionId => "invalid_auction_id"
      case LossReason.InvalidAdDomain => "invalid_ad_domain"
      case LossReason.MissingMarkup => "missing_markup"
      case LossReason.MissingCreativeId => "missing_creative_id"
      case LossReason.MissingBidPrice => "missing_bid_price"
      case LossReason.MissingMinCreativeApprovalData => "missing_min_creative_approval_data"
      case LossReason.BidBelowAuctionFloor => "bid_below_auction_floor"
      case LossReason.BidBelowDealFloor => "bid_below_deal_floor"
      case LossReason.LostToHigherBid => "loss_to_higher_bid"
      case LossReason.LostToBidForPMPDeal => "lost_to_bid_for_pmp_deal"
      case LossReason.BuyerSeatBlocked => "buyer_seat_blocked"
      case LossReason.CreativeFilteredGeneral => "creative_filtered_general"
      case LossReason.CreativeFilteredPendingApproval => "creative_filtered_pending_approval"
      case LossReason.CreativeFilteredDisapproved => "creative_filtered_disapproved"
      case LossReason.CreativeFilteredSizeNotAllowed => "creative_filtered_size_not_allowed"
      case LossReason.CreativeFilteredIncorrectCreativeFormat => "creative_filtered_incorrect_creative_format"
      case LossReason.CreativeFilteredAdvertiserExclusions => "creative_filtered_advertiser_exclusions"
      case LossReason.CreativeFilteredAppBundleExclusions => "creative_filtered_app_bundle_exclusions"
      case LossReason.CreativeFilteredNotSecure => "creative_filtered_not_secure"
      case LossReason.CreativeFilteredLanguageExclusions => "creative_filtered_language_exclusions"
      case LossReason.CreativeFilteredCategoryExclusions => "creative_filtered_category_exclusions"
      case LossReason.CreativeFilteredCreativeAttributeExclusions => "creative_filtered_creative_attribute_exclusions"
      case LossReason.CreativeFilteredAdTypeExclusions => "creative_filtered_ad_type_exclusions"
      case LossReason.CreativeFilteredAnimationTooLong => "creative_filtered_animation_too_long"
      case LossReason.CreativeFilteredNotAllowedInPMPDeal => "creative_filtered_not_allowed_in_pmp_deal"
    }
  }

  implicit class NoBidReasonOps(nbr: openrtb.NoBidReason) extends Pretty {
    override def prettyValue: String = nbr match {
      case NoBidReason.UnknownError => "unknown_error"
      case NoBidReason.TechnicalError => "technical_error"
      case NoBidReason.InvalidRequest => "invalid_request"
      case NoBidReason.KnownWebSpider => "known_web_spider"
      case NoBidReason.SuspectedNonHumanTraffic => "suspected_non_human_traffic"
      case NoBidReason.ProxyIP => "proxy_ip"
      case NoBidReason.UnsupportedDevice => "unsupported_device"
      case NoBidReason.BlockedPublisherOrSite => "blocked_publisher_or_site"
      case NoBidReason.UnmatchedUser => "unmatched_user"
      case NoBidReason.DailyReaderCap => "daily_reader_cap"
      case NoBidReason.DailyDomainCap => "daily_domain_cap"
    }
  }

  implicit class PlaybackCessationModeOps(pcm: openrtb.PlaybackCessationMode) extends Pretty {
    override def prettyValue: String = pcm match {
      case PlaybackCessationMode.OnCompletion => "on_completion"
      case PlaybackCessationMode.OnLeavingViewport => "on_leaving_viewport"
      case PlaybackCessationMode.FloatUntilCompletion => "float_until_completion"
    }
  }

  implicit class PlaybackMethodOps(pm: openrtb.PlaybackMethod) extends Pretty {
    override def prettyValue: String = pm match {
      case PlaybackMethod.AutoPlaySoundOn => "auto_play_sound_on"
      case PlaybackMethod.AutoPlaySoundOff => "auto_play_sound_off"
      case PlaybackMethod.ClickToPlay => "click_to_play"
      case PlaybackMethod.MouseOver => "mouse_over"
      case PlaybackMethod.OnViewportEnterSoundOn => "on_viewport_enter_sound_on"
      case PlaybackMethod.OnViewportEnterSoundOff => "on_viewport_enter_sound_off"
    }
  }

  implicit class ProductionQualityOps(pq: openrtb.ProductionQuality) extends Pretty {
    override def prettyValue: String = pq match {
      case ProductionQuality.Unknown => "unknown"
      case ProductionQuality.ProfessionallyProduced => "professionally_produced"
      case ProductionQuality.Prosumer => "prosumer"
      case ProductionQuality.UserGenerated => "user_generated"
    }
  }

  implicit class ProtocolOps(p: openrtb.Protocol) extends Pretty {
    override def prettyValue: String = p match {
      case Protocol.VAST_1 => "vast_1"
      case Protocol.VAST_2 => "vast_2"
      case Protocol.VAST_3 => "vast_3"
      case Protocol.VAST_1_WRAPPER => "vast_1_wrapper"
      case Protocol.VAST_2_WRAPPER => "vast_2_wrapper"
      case Protocol.VAST_3_WRAPPER => "vast_3_wrapper"
      case Protocol.VAST_4 => "vast_4"
      case Protocol.VAST_4_WRAPPER => "vast_4_wrapper"
      case Protocol.DAAST_1 => "daast_1"
      case Protocol.DAAST_1_WRAPPER => "daast_1_wrapper"
    }
  }

  implicit class QagMediaRatingOps(qmr: openrtb.QagMediaRating) extends Pretty {
    override def prettyValue: String = qmr match {
      case QagMediaRating.AllAudiences => "all_audiences"
      case QagMediaRating.EveryoneOver12 => "everyone_over_12"
      case QagMediaRating.MatureAudiences => "mature_audiences"
    }
  }

  implicit class VideoLinearityOps(vl: openrtb.VideoLinearity) extends Pretty {
    override def prettyValue: String = vl match {
      case VideoLinearity.Linear => "linear"
      case VideoLinearity.NonLinear => "non_linear"
    }
  }

  implicit class VideoPlacementTypeOps(vpt: openrtb.VideoPlacementType) extends Pretty {
    override def prettyValue: String = vpt match {
      case VideoPlacementType.InStream => "in_stream"
      case VideoPlacementType.InBanner => "in_banner"
      case VideoPlacementType.InArticle => "in_article"
      case VideoPlacementType.InFeed => "in_feed"
      case VideoPlacementType.Interstitial => "interstitial"
    }
  }

  implicit class VolumeNormalizationModeOps(vnm: openrtb.VolumeNormalizationMode) extends Pretty {
    override def prettyValue: String = vnm match {
      case VolumeNormalizationMode.None => "none"
      case VolumeNormalizationMode.AdVolumeAverageNormalizedToContent => "volume_avg"
      case VolumeNormalizationMode.AdVolumePeakNormalizedToContent => "volume_peak"
      case VolumeNormalizationMode.AdLoudnessNormalizedToContent => "loudness"
      case VolumeNormalizationMode.CustomVolumeNormalization => "custom"
    }
  }

  implicit class ContextSubtypeOps(cs: openrtb.native.ContextSubtype) extends Pretty {
    override def prettyValue: String = cs match {
      case ContextSubtype.MixedContent => "mixed_content"
      case ContextSubtype.Article => "article"
      case ContextSubtype.Video => "video"
      case ContextSubtype.Audio => "audio"
      case ContextSubtype.Image => "image"
      case ContextSubtype.UserGenerated => "user_generated"
      case ContextSubtype.Social => "social"
      case ContextSubtype.Email => "email"
      case ContextSubtype.Chat => "chat"
      case ContextSubtype.Market => "market"
      case ContextSubtype.AppStore => "application_store"
      case ContextSubtype.ProductReviews => "product_review"
    }
  }

  implicit class ContextTypeOps(ct: openrtb.native.ContextType) extends Pretty {
    override def prettyValue: String = ct match {
      case ContextType.ContentCentric => "content_centric"
      case ContextType.SocialCentric => "social_centric"
      case ContextType.ProductContext => "product_context"
    }
  }

  implicit class DataTypeOps(dt: openrtb.native.DataType) extends Pretty {
    override def prettyValue: String = dt match {
      case DataType.Sponsored => "sponsored"
      case DataType.Desc => "desc"
      case DataType.Rating => "rating"
      case DataType.Likes => "likes"
      case DataType.Download => "downloads"
      case DataType.Price => "price"
      case DataType.SalePrice => "sale_price"
      case DataType.Phone => "phone"
      case DataType.Address => "address"
      case DataType.Desc2 => "desc2"
      case DataType.DisplayUrl => "display_url"
      case DataType.CtaText => "cta_text"
    }
  }

  implicit class ImageTypeOps(it: openrtb.native.ImageType) extends Pretty {
    override def prettyValue: String = it match {
      case ImageType.Icon => "icon"
      case ImageType.Logo => "logo" // deprecated
      case ImageType.Main => "main"
    }
  }

  implicit class PlacementTypeOps(pt: openrtb.native.PlacementType) extends Pretty {
    override def prettyValue: String = pt match {
      case PlacementType.Feed => "feed_content"
      case PlacementType.Atomic => "atomic_content"
      case PlacementType.Outside => "outside_content"
      case PlacementType.Recommendation => "recommendation_widget"
    }
  }

}
