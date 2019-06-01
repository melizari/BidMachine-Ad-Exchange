package services.auction.pb.adapters.pubnative.model

/**
 * PubNative Ad object
 *
 * @param link    Click through URL
 * @param assets  Asset object contains information of all assets requested via the 'al' parameter.
 * @param meta    Meta object contains information of all meta properties requested via the 'mf' parameter
 * @param beacons Beacons Object contains URL beacon used to confirm impressions
 */
case class PubNativeAd(link: Option[String],
                       assetgroupid: AssetGroupId,
                       assets: List[PubNativeAsset],
                       meta: Option[List[Meta]],
                       beacons: List[Beacon])
