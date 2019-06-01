package services.auction.pb.adapters.rubicon.model

import com.appodealx.exchange.common.models.NativeRequest
import com.appodealx.openrtb.native.LayoutType
import com.appodealx.openrtb.native.request.Asset
import io.circe.Json


case class RubiconNativeRequest(layout: LayoutType,
                                assets: List[Asset],
                                ver: Option[String] = None,
                                adUnit: Option[Int] = None,
                                seq: Option[Int] = None,
                                ext: Option[Json] = None)

object RubiconNativeRequest {

  private val defaultNative = NativeRequest.Default

  val Default = RubiconNativeRequest(
    LayoutType.ContentStream,
    defaultNative.assets,
    ver = defaultNative.ver,
    seq = defaultNative.seq
  )
}
